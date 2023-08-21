package de.zeroco.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.zeroco.core.dao.FeedDao;
import de.zeroco.core.util.ZcMap;
import de.zeroco.core.util.ZcUtil;

@Component
public class FeedService {
	
	@Autowired
	private FeedDao dao;
	
	public ZcMap feedSave(ZcMap reqData) throws Exception {
		ZcMap resData = new ZcMap();
		try {
			Boolean status = reqData.getB("status");
			String env = reqData.getS("environment");
			String client = reqData.getS("client");
			String application = reqData.getS("application");
			String host = reqData.getS("host");
			String serverStatus = status ? "success" : "failed";
			String lastUpdatedTime = reqData.getS("accessDateTime");
			String firstSuccessOn = serverStatus.equalsIgnoreCase("success") ? lastUpdatedTime : null;
			String firstFailureOn = serverStatus.equalsIgnoreCase("failed") ? lastUpdatedTime : null;
			reqData.put("serverStatus",serverStatus);
			ZcMap data = dao.get(env, client, application, host);
			if (ZcUtil.isBlank(data)) {
				dao.feedSave(env, client, application, host, serverStatus, firstSuccessOn, firstFailureOn,
						lastUpdatedTime, true);
				reqData.put("first_success_on", firstSuccessOn); reqData.put("first_failure_on", firstFailureOn); reqData.put("last_updated", lastUpdatedTime); reqData.put("restart", true);
				return reqData;
			} else if (!data.getS("status").equals(serverStatus)) {
				reqData.put("first_failure_on", data.get("first_failure_on")); reqData.put("first_success_on", data.get("first_success_on"));
				if (serverStatus.equals("failed") && !data.getS("status").equals("failed")) {
					dao.updateFailedStatus(serverStatus, firstFailureOn, lastUpdatedTime, env, client, application,host);
					reqData.put("first_failure_on", firstFailureOn);
				} else if (serverStatus.equals("success") && ZcUtil.isBlank(data.get("first_success_on"))) {
					dao.updateFirstSuccessStatus(serverStatus, firstSuccessOn, lastUpdatedTime, env, client,application, host);
					reqData.put("first_success_on", firstSuccessOn);
				} else
					dao.updateStatus(serverStatus, lastUpdatedTime, env, client, application, host);
				dao.feedSaveAduit(env, client, application, host, serverStatus, firstSuccessOn, firstFailureOn,lastUpdatedTime);
				reqData.put("restart", data.getB("restart")); reqData.put("last_updated", lastUpdatedTime);
				return reqData;
			}
			dao.updateTime(lastUpdatedTime, env, client, application, host);
			data.put("serverStatus",serverStatus);
			data.put("last_updated", lastUpdatedTime);
			return data;
		} catch (Exception e) {
			resData.put("error", "Error Occured !");
			resData.put("msg", e.getMessage());
			return resData;
		}
		
	}

}
