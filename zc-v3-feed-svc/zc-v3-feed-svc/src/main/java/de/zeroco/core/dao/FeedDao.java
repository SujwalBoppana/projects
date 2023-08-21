package de.zeroco.core.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import de.zeroco.core.util.ZcMap;
import de.zeroco.core.util.ZcUtil;

@Component
public class FeedDao {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private Map<String, Object> processSqlResultsOfMap(Map<String, Object> x) {
		for (Map.Entry<String, Object> entry : x.entrySet()) {
			String k = entry.getKey();
			Object v = entry.getValue();
			if (v instanceof java.sql.Timestamp)
				x.put(k, ZcUtil.getDateTimeString((java.sql.Timestamp) v));
		}
		return x;
	}

	public int getGeneratedKeys(String query, Object... args) {
		KeyHolder holder = new GeneratedKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
				for (int i = 0; i < args.length; i++)
					ps.setObject(i + 1, args[i]);
				return ps;
			}
		}, holder);
		return holder.getKey().intValue();
	}

	public ZcMap get(String env, String client, String application, String host) {
		try {
			String query = "SELECT * FROM `server` WHERE (`environment` = ? AND `client` = ? AND `application` = ? AND `host` = ? );";
			Map<String, Object> resData = processSqlResultsOfMap(
					jdbcTemplate.queryForMap(query, env, client, application, host));
			ZcMap o = new ZcMap();
			o.putAll(resData);
			return o;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public int feedSave(String env, String client, String application, String host, String status,
			String firstSuccessOn, String firstFailureOn, String lastUpdated, boolean refresh) {
		String query = "INSERT INTO `server` (`environment`,`client`,`application`,`host`,`status`,`first_success_on`,`first_failure_on`,`last_updated`,`restart`) VALUES (?,?,?,?,?,?,?,?,?);";
		return getGeneratedKeys(query, env, client, application, host, status, firstSuccessOn, firstFailureOn,
				lastUpdated, refresh);

	}

	public int feedSaveAduit(String env, String client, String application, String host, String status,
			String firstSuccessOn, String firstFailureOn, String lastUpdated) {
		String query = "INSERT INTO `server_audit` (`environment`,`client`,`application`,`host`,`status`,`first_success_on`,`first_failure_on`,`last_updated`)  VALUES (?,?,?,?,?,?,?,?);";
		return getGeneratedKeys(query, env, client, application, host, status, firstSuccessOn, firstFailureOn,
				lastUpdated);
	}

	public int updateFailedStatus(String status, String firstFailureOn, String lastUpdated, String env, String client,
			String application, String host) {
		String query = "UPDATE `server` SET `status` = ? ,`first_failure_on` = ? ,`last_updated` = ?  WHERE (`environment` = ? AND `client` = ? AND `application` = ? AND `host` = ? );";
		return jdbcTemplate.update(query, status, firstFailureOn, lastUpdated, env, client, application, host);
	}

	public int updateFirstSuccessStatus(String status, String firstSuccessOn, String lastUpdated, String env,
			String client, String application, String host) {
		String query = "UPDATE `server` SET `status` = ? ,`first_success_on` = ? ,`last_updated` = ?  WHERE (`environment` = ? AND `client` = ? AND `application` = ? AND `host` = ? );";
		return jdbcTemplate.update(query, status, firstSuccessOn, lastUpdated, env, client, application, host);
	}

	public int updateStatus(String status, String lastUpdated, String env, String client, String application,
			String host) {
		String query = "UPDATE `server` SET `status` = ? ,`last_updated` = ?  WHERE (`environment` = ? AND `client` = ? AND `application` = ? AND `host` = ? );";
		return jdbcTemplate.update(query, status, lastUpdated, env, client, application, host);
	}

	public int updateTime(String lastUpdated, String env, String client, String application, String host) {
		String query = "UPDATE `server` SET `last_updated` = ?  WHERE (`environment` = ? AND `client` = ? AND `application` = ? AND `host` = ? );";
		return jdbcTemplate.update(query, lastUpdated, env, client, application, host);
	}
}
