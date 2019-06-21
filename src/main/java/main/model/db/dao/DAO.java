package main.model.db.dao;

import com.mysql.cj.core.conf.url.ConnectionUrlParser.Pair;
import main.exceptions.RPException;
import main.model.db.RS_Converter;
import main.model.dto.BaseDto;
import main.model.dto.DtoMapper;
import org.json.JSONArray;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class DAO<T extends BaseDto> {
    private Connection connection;
    private Class<T> type;
    protected DtoMapper<T> dtoMapper;
    protected String select;
    protected String insert;
    protected String remove;

    protected DAO(Class<T> type){
        this.type = type;
        dtoMapper = new DtoMapper<>(type);
    }

    public boolean deleteMultiply(List<T> entities){
        boolean success = true;
        for (T entity: entities){
            try {
                delete(entity);
            } catch (RPException e) {
                e.printStackTrace();
                success = false;
            }
        }
        return success;
    }

    public List<T> getAll() throws RPException {
        try {
            return searchAll(type.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RPException("Cannot Create new Instance of " + type.getName());
        }
    }

    public List<T> searchAll(T entity) throws RPException {
        List<Pair<String, String>> parameters = entity.getSearchParameters();

        return dtoMapper.mapObjects(CallStoredProcedure(select, parameters).toString());
    }

    public T getEntityById(T entity) throws RPException {
        return searchAll(entity).get(0);
    }

    public T update(T entity) throws RPException {
        return create(entity);
    }

    public boolean delete(T entity) throws RPException {
        List<Pair<String, String>> parameters = entity.getIDParameters();

        CallStoredProcedure(remove, parameters);
        return true;
    }

    public T create(T entity) throws RPException {
        List<Pair<String, String>> parameters = entity.getParameters();

        return dtoMapper.mapObjects(CallStoredProcedure(insert, parameters).toString()).get(0);
    }

    public boolean createMultiply(List<T> entities) throws RPException {
        for (T entity: entities ) {
            create(entity);
        }
        return true;
    }

    public boolean updateMultiply(List<T> entities) throws RPException {
        for (T entity: entities ) {
            update(entity);
        }
        return true;
    }

    protected JSONArray CallStoredProcedure(String sql, List<Pair<String, String>> parameters) throws RPException {
        JSONArray json = null;
        CallableStatement callableStatement = executeCallableStatement(sql, parameters, null);
        try{
            ResultSet rs = callableStatement.getResultSet();
            if (rs != null) {
                json = RS_Converter.convertToJSON(rs);
                rs.close();
            }
        } catch (SQLException e) {
            throw new RPException(String.format("Cannot execute statement for '%s'", getSqlName(sql)));
        } finally {
            closeCallableStatement(callableStatement);
        }
        return json;
    }

    private void getConnection() throws RPException{
        InitialContext initialContext;
        try {
            initialContext = new InitialContext();
            DataSource dataSource = (DataSource) initialContext.lookup("java:/comp/env/jdbc/URDB");
            connection = dataSource.getConnection();
        } catch (NamingException | SQLException e) {
            throw new RPException(String.format("Cannot get Connection: %s", e.getMessage()));
        }
    }

    private void returnConnectionInPool() throws SQLException {
        connection.close();
    }

    private CallableStatement executeCallableStatement(String sql, List<Pair<String, String>> parameters, List<Pair<String, Integer>> output) throws RPException {
        CallableStatement callableStatement = getCallableStatement(sql);

        if(callableStatement == null){
            throw new RPException(String.format("Cannot create statement for '%s'", getSqlName(sql)));
        }

        if (parameters != null) {
            for (Pair<String, String> parameter : parameters) {
                try{
                    callableStatement.setString(parameter.left, parameter.right);
                } catch (NullPointerException e){
                    throw new RPException(String.format("The %s parameter is not registered in stored procedure: '%s'", parameter.left, getSqlName(sql)));
                } catch (SQLException e) {
                    throw new RPException(String.format("Cannot set %s parameter to stored procedure: %s", parameter.right, e.getMessage()));
                }
            }
        }

        if (output != null) {
            for (Pair<String, Integer> out : output) {
                try {
                    callableStatement.registerOutParameter(out.left, out.right);
                } catch (SQLException e) {
                    throw new RPException(String.format("Cannot register out parameter for '%s':", getSqlName(sql)));
                }
            }
        }

        try {
            callableStatement.execute();
        } catch (SQLException e) {
            throw new RPException(String.format("Cannot execute query '%s': %s", getSqlName(sql), e.getMessage()));
        }

        return callableStatement;
    }

    private CallableStatement getCallableStatement(String sql) throws RPException {
        getConnection();
        CallableStatement callableStatement;
        try {
            callableStatement = connection.prepareCall(sql);
        } catch (SQLException e) {
            throw new RPException(String.format("Cannot create statement for '%s'", sql));
        }

        return callableStatement;
    }

    private void closeCallableStatement(CallableStatement callableStatement) throws RPException {
        if (callableStatement != null) {
            try {
                callableStatement.close();
                returnConnectionInPool();
            } catch (SQLException e) {
                throw new RPException("Cannot close callable statement");
            }
        }
    }

    private String getSqlName(String storedProcedure){
        Pattern pattern = Pattern.compile(".*call (.*)\\(.*\\}");
        Matcher matcher = pattern.matcher(storedProcedure);
        if(matcher.find()){
            String match = matcher.group(1);
            return match.replaceAll("_", " ");
        }
        return "";
    }
}
