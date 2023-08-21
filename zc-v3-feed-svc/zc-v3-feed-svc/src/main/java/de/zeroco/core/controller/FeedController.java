package de.zeroco.core.controller;

import java.net.InetAddress;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import de.zeroco.core.service.FeedService;
import de.zeroco.core.util.ZcMap;
import de.zeroco.core.util.ZcUtil;
import de.zeroco.core.validation.FeedValidation;

@RestController
public class FeedController {
	
	
	@Autowired
	private  FeedService service;
	
	@SuppressWarnings("serial")
	@RequestMapping(value = "/saveFeed", method = RequestMethod.POST)
	public  ResponseEntity<ZcMap> feedSave(@RequestBody ZcMap reqData) throws Exception {
		reqData.put("accessDateTime",ZcUtil.getDateTimeString(new Date()));
		if(ZcUtil.isBlank(reqData)||!FeedValidation.requiredFields(reqData, "environment","client","application","host")) {
			return new ResponseEntity<ZcMap>( new ZcMap() {{
				put("success",false);
				put("msg","In Valid data Or Required Fields are Missing");
				}}, HttpStatus.BAD_REQUEST);
		}
		ZcMap resData = service.feedSave(reqData);
		if (resData.containsKey("error")) {
			return new ResponseEntity<ZcMap>(new ZcMap() {{
					put("success", false);
					put("msg", resData.getS("msg"));
				}}, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<ZcMap>(new ZcMap() {{
			put("success",true);
			put("status",resData.getS("serverStatus"));
			if(reqData.getB("status")) {
				put("first_success_on",resData.getS("first_success_on"));
				put("success_duration",ZcUtil.getDiffMins(resData.getS("first_success_on"), resData.getS("last_updated")));
			}else {
				put("first_failure_on",resData.getS("first_failure_on"));
				put("failure_duration",ZcUtil.getDiffMins(resData.getS("first_failure_on"), resData.getS("last_updated")));
			}
			put("last_updated",resData.getS("last_updated"));
			put("restart",resData.getB("restart"));
		}},HttpStatus.OK);
	}
	
	
	@SuppressWarnings("serial")
	@RequestMapping(value = "/getFeed", method = RequestMethod.GET)
	public ResponseEntity<ZcMap> get() {
		ZcMap resData = new ZcMap();
		try {
			resData.put("zcServerDateTime", ZcUtil.getDateTimeString(new Date()));
			resData.put("zcServerIp", InetAddress.getLocalHost().toString());
			resData.put("zcServerHost", InetAddress.getLocalHost().getHostName());
		} catch (Exception e) {
		}
		return new ResponseEntity<ZcMap>(new ZcMap() {{
				put("success", true);
				putAll(resData);
			}}, HttpStatus.OK);
	}

}
