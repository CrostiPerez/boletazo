package com.itq.progradist.boletazo.controladores;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.itq.progradist.boletazo.modelos.Asiento;
import com.itq.progradist.boletazo.modelos.Evento;
import com.itq.progradist.boletazo.modelos.Zona;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class ControladorEvento {
	
	/**
	 * logger del servidor, escribe en server.log
	 */
	private static final Logger logger = LogManager.getLogger(ControladorEvento.class);
	
	/**
	 * Conexi�n a la base de datos
	 */
	private Connection conexion;
	
	/**
	 * datos de la petici�n
	 */
	private JSONObject dataRequest;
	
	/**
	 * Inicializar un controlador con una conexi�n a la base de datos y
	 * datos de petici�n
	 * 
	 * @param conexion Conexi�n a la base de datos
	 * @param dataRequest Par�metros de la petici�n
	 */
	public ControladorEvento(Connection conexion, JSONObject dataRequest) {
		super();
		this.conexion = conexion;
		this.dataRequest = dataRequest;
	}

	/**
	 * Devuelve datos consultados de la base de datos seg�n
	 * el m�todo que indiquen los par�metros
	 * 
	 * @param params Par�metros de la petici�n, debe contener el m�todo de la petici�n
	 * @return respuesta Respuesta obtenida de la base de datos
	 */
	public JSONObject procesarAccion(JSONObject params) {
		logger.info("Procesando acci�n");
		JSONObject respuesta = new JSONObject();
		try {
			switch (params.getString("metodo")) {
			case "get":
				logger.info("Obteniendo eventos");
				respuesta.put("data", this.getEventos(params));
				logger.info("Eventos obtenidos");
				return respuesta;
			default:
				throw new IllegalArgumentException("Unexpected value: " + params.get("method"));
			}
		} catch (IllegalArgumentException e) {
			logger.error("Error procesando la acci�n" + e.getMessage());
		}
		return null;
	}
	
	/**
	 * Obtener un evento por su ID
	 * @param idEvento
	 * @return
	 */
	private Evento getEvento(int idEvento) {
		return null;
	}
	
	/**
	 * Obtiene eventos de la base de datos seg�n los par�metros dados
	 * 
	 * @param params Parametros de b�squeda de los eventos
	 * @return respuesta Eventos que coicidieron con los par�metros
	 */
	private JSONArray getEventos(JSONObject params) {
		logger.info("Iniciando consulta en la base de datos");
		Statement stmt = null;
		String sql = getEventosSqlQuery(params);
		JSONArray respuesta = new JSONArray();
		try {
			stmt = this.conexion.createStatement();
			logger.info("Ejecutando consulta");
			ResultSet rs = stmt.executeQuery(sql);
			while(rs.next()){
		         Evento evento = new Evento(
		        		 rs.getInt("idEvento"), 
		        		 rs.getInt("idLugar"), 
		        		 rs.getString("nombre"),
		        		 rs.getString("fecha"),
		        		 rs.getString("hora")
	        		 );
		         Gson gson = new Gson();
		         respuesta.put(gson.toJson(evento));
			}
			logger.info("Datos obtenidos de la base de datos");
			return respuesta;
		} catch (SQLException e) {
			logger.error("Error al consultar la base de datos: " + e.getMessage());
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Devuelve la consulta SQL de los eventos que
	 * cumplan con lo par�metros seleccionados
	 * 
	 * @param params Par�mteros de la consulta
	 * @return sql Consulta a la base de datos
	 */
	private String getEventosSqlQuery(JSONObject params) {
		
		String sql = "SELECT Eventos.* FROM Zona, Lugar, Eventos"
				+ " WHERE Zona.idLugar = Lugar.idLugar"
				+ " AND Lugar.idEvento = Eventos.idEvento";
		
		if (params.has("nombre")) {
			sql += " AND Eventos.nombre LIKE '%" + params.getString("nombre") + "%'";
		}
		if (params.has("lugar")) {
			sql += " AND Lugar.nombre LIKE '%" + params.getString("lugar") + "%'";
		}
		if (params.has("estado")) {
			sql += " AND Lugar.estado LIKE '%" + params.getString("estado") + "%'";
		}
		if (params.has("fecha")) {
			sql += " AND Eventos.fecha LIKE '%" + params.getString("fecha") + "%'";
		}
		if (params.has("hora")) {
			sql += " AND Eventos.hora LIKE '%" + params.getString("hora") + "%'";
		}
		if (params.has("precio")) {
			sql += " AND Zona.precio LIKE '%" + params.getDouble("precio") + "%'";
		}
		
		sql += " GROUP BY Eventos.idEvento";
		
		return sql;
	}
	
}
