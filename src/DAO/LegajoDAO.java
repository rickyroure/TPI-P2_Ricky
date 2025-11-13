/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package Dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import Config.DatabaseConnection;
import Main.Estado;
import Models.Legajo;

public class LegajoDAO implements GenericDAO<Legajo> {
      
    // 1. QUERYS
    private static final String INSERT_SQL = "INSERT INTO legajo (nro_legajo, categoria, estado, fecha_alta, observaciones, empleado_id, eliminado) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_CATEGORIA = "UPDATE legajo SET categoria = ? WHERE nro_legajo = ?"; 
    
    private static final String UPDATE_ESTADO = "UPDATE legajo SET estado = ? WHERE nro_legajo = ? AND eliminado = FALSE";
    private static final String DELETE_SQL = "UPDATE legajo SET eliminado = TRUE WHERE nro_legajo = ? AND eliminado = FALSE";
    
    private static final String SEARCH_BY_ID = "SELECT * FROM legajo WHERE id = ? AND eliminado = FALSE";
    private static final String SELECT_ALL_ACTIV = "SELECT * FROM legajo WHERE eliminado = FALSE AND UPPER(estado) = 'ACTIVO'";
    
    public LegajoDAO() {} 
        
   
    @Override
    public void insertar(Legajo legajo) throws SQLException {
        throw new UnsupportedOperationException("Use insertTx pasando el ID del empleado.");
    }
       
  
    public void insertTx(Legajo legajo, Long empleadoId, Connection conex) throws SQLException {
        try (PreparedStatement stmt = conex.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            
            setLegajoParameters(stmt, legajo);   
            
       
            stmt.setLong(6, empleadoId); 
            stmt.setBoolean(7, false); 

            stmt.executeUpdate();
            
         
        }
    }

    @Override
    public void insertTx(Legajo legajo, Connection conex) throws SQLException {
        throw new UnsupportedOperationException("Falta el ID del empleado. Use la sobrecarga: insertTx(legajo, empleadoId, conex)");
    }

    @Override
    public void actualizar(Legajo legajo) throws SQLException {
         try (Connection conex = DatabaseConnection.getConnection();
            PreparedStatement stmt = conex.prepareStatement(UPDATE_CATEGORIA)) {
            
            stmt.setString(1, legajo.getCategoria());
            stmt.setString(2, legajo.getNroLegajo()); 
            
            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new SQLException("No se pudo actualizar el legajo: " + legajo.getNroLegajo());
            }
        }
    }
       
    @Override
    public void eliminar(int id) throws SQLException {
        String sqlDeleteById = "UPDATE legajo SET eliminado = TRUE WHERE id = ? AND eliminado = FALSE";
        
        try (Connection conex = DatabaseConnection.getConnection();
             PreparedStatement stmt = conex.prepareStatement(sqlDeleteById)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("El legajo no existe o ya estaba eliminado.");
            }
        }
    }
  
    @Override
    public Legajo leer(int id) throws SQLException {
        try (Connection conex = DatabaseConnection.getConnection();
                PreparedStatement stmt = conex.prepareStatement(SEARCH_BY_ID)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLegajo(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Legajo> leerTodos() throws SQLException {
        List<Legajo> listaLegajos = new ArrayList<>();
        try(Connection conex = DatabaseConnection.getConnection(); 
                PreparedStatement stmt = conex.prepareStatement(SELECT_ALL_ACTIV);
                ResultSet rs = stmt.executeQuery()){
                
            while (rs.next()) {
                listaLegajos.add(mapResultSetToLegajo(rs));
            }
        }
        return listaLegajos;
    }
    
    
         
    private void setLegajoParameters(PreparedStatement stmt, Legajo legajo) throws SQLException {
        
        stmt.setString(1, legajo.getNroLegajo());

        if (legajo.getCategoria() != null) {
            stmt.setString(2, legajo.getCategoria());
        } else {
            stmt.setNull(2, Types.VARCHAR);
        }

 
        stmt.setString(3, legajo.getEstado() != null ? legajo.getEstado().name() : Estado.ACTIVO.name());


        if (legajo.getFechaAlta() != null) {
            stmt.setDate(4, java.sql.Date.valueOf(legajo.getFechaAlta()));
        } else {
            stmt.setNull(4, Types.DATE);
        }

        if (legajo.getObservaciones() != null) {
            stmt.setString(5, legajo.getObservaciones());
        } else {
            stmt.setNull(5, Types.VARCHAR);
        }
    }

    private Legajo mapResultSetToLegajo(ResultSet rs) throws SQLException {
  
        return new Legajo(
            rs.getString("nro_legajo"),
            rs.getBoolean("eliminado"),
            rs.getString("categoria"),
            Estado.valueOf(rs.getString("estado")),
            rs.getDate("fecha_alta") != null ? rs.getDate("fecha_alta").toLocalDate() : null,
            rs.getString("observaciones")
        );
    }
}
