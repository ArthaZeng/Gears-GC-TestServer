package org.gears.network;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;

import org.gears.DataObject;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

/*
 * test data
 * 
 * broadcasting:
 * {"action":"broadcasting", "variables":"test", "timestamp":"null", "body":"testing"}
 * 
 * set_user
 * {"action":"set_user", "variables":"test", "timestamp":"null", "body":"TestUser"}
 * 
 * add_user
 * {"action":"add_user", "variables":"test", "timestamp":"null", "body":"TestUser"}
 * 
 * set_object
 * {"action":"set_object", "variables":{"key":"MyObj", "autoSync":"true"}, "timestamp":"null", "body":"TestObject"}
 * 
 * get_object
 * {"action":"get_object", "variables":{"key":"MyObj", "autoSync":"true"}, "timestamp":"null", "body":""} 
 * 
 * create_list
 * {"action":"create_list", "variables":{"key":"MyList", "autoSync":"true"}, "timestamp":"null", "body":""}
 * 
 * get_list
 * *not done yet ;p
 * {"action":"get_list", "variables":{"key":"MyList", "autoSync":"true"}, "timestamp":"null", "body":""}
 * 
 * append_list_item
 * {"action":"append_list_item", "variables":{"key":"MyList", "autoSync":"true"}, "timestamp":"null", "body":"test1"}
 * 
 * get_list_item
 * * not fully done yet
 * {"action":"get_list_item", "variables":{"key":"MyList", "autoSync":"true"}, "timestamp":"null", "body":"0"}
 * 
 * remove_list_item
 * {"action":"remove_list_item", "variables":{"key":"MyList", "autoSync":"true"}, "timestamp":"null", "body":"test1"}
 * 
 * removeListItemByIndex
 * {"action":"remove_list_item_by_index", "variables":{"key":"MyList", "autoSync":"true"}, "timestamp":"null", "body":"0"}
 * 
 */

public class GCCommunicationServer extends WebSocketServer {

	//private HashMap<WebSocket, GCUser> userList;
	private HashMap<String, String> gcObjects;
	private HashMap<String, ArrayList<String>> gcLists;
	
	public GCCommunicationServer( int port ) throws UnknownHostException {
		super( new InetSocketAddress( port ) );
		this.initialize();
	}

	public GCCommunicationServer( InetSocketAddress address ) {
		super( address );
		this.initialize();
	}
	
	private void initialize()
	{
		//this.userList = new HashMap<WebSocket, GCUser>();
		this.gcObjects = new HashMap<String, String>();
		this.gcLists = new HashMap<String, ArrayList<String>>();
	}
	
	@Override
	public void onClose(WebSocket arg0, int arg1, String arg2, boolean arg3)
	{
//		if (this.userList.containsKey(arg0))
//		{
//			this.userList.remove(arg0);
//		}
//		
//		this.broadcastUserList();
		//TODO
		//when user leave the game, i.e. close connection
		//what to do
	}

	@Override
	public void onError(WebSocket arg0, Exception arg1)
	{

	}

	@Override
	public void onMessage(WebSocket sourceSocket, String data)
	{
		//TODO test
		DataObject obj1 = new DataObject();
//		obj1.setAction("testAction");
//		obj1.setBody("testBody");
//		obj1.setTimestamp("bla");
//		obj1.setVariables("testVar");
//		this.broadcast(sourceSocket, obj1.getJson(), false);
		
		DataObject obj = new DataObject();
		obj.parseJson(data);
//		System.out.println(data);
//		System.out.println(obj);
//		System.out.println(obj.getBody());
		
		if (obj.getAction().equals("broadcasting"))
		{
			this.broadcast(sourceSocket, data, false);
		}
		else if (obj.getAction().equals("set_user") || obj.getAction().equals("add_user"))
		{
			this.addUser(sourceSocket, data);
		}
		else if (obj.getAction().equals("set_object"))
		{
			this.updateGcObjects(sourceSocket, data);
		}
		else if (obj.getAction().equals("get_object"))
		{
			this.sendGcObjects(sourceSocket, data);
		}
		else if (obj.getAction().equals("create_list"))
		{
			this.createGcList(sourceSocket, data);
		}
		else if (obj.getAction().equals("get_list")){
			this.sendGcList(sourceSocket, data);
		}
		else if (obj.getAction().equals("append_list_item")){
			this.appendGcList(sourceSocket, data);
		}
		else if (obj.getAction().equals("get_list_item")){
			this.sendGcListItem(sourceSocket, data);
		}
		else if (obj.getAction().equals("get_list_index")){
			this.getItemIndex(sourceSocket, data);
		}
		else if (obj.getAction().equals("remove_list_item")){
			this.removeListItem(sourceSocket, data);
		}
		else if (obj.getAction().equals("remove_list_item_by_index")){
			this.removeListItemByIndex(sourceSocket, data);
		}
	}
	
	public void removeListItemByIndex(WebSocket sourceSocket, String data){
		DataObject obj = new DataObject();
		obj.parseJson(data);
		String key = obj.getVariableKey();
		String body = obj.getBody();
		int index = Integer.parseInt(body);
		
		gcLists.get(key).remove(index);
		
		if(obj.getVariableAutoSync().equals("true")){
			this.broadcast(sourceSocket, data, false);
		}
	}
	
	public void removeListItem(WebSocket sourceSocket, String data){
		DataObject obj = new DataObject();
		obj.parseJson(data);
		String key = obj.getVariableKey();
		String body = obj.getBody();
		
		gcLists.get(key).remove(body);
		
		if(obj.getVariableAutoSync().equals("true")){
			this.broadcast(sourceSocket, data, false);
		}
	}
	
	public void getItemIndex(WebSocket sourceSocket, String data){
		DataObject obj = new DataObject();
		obj.parseJson(data);
		String key = obj.getVariableKey();
		String body = obj.getBody();
		
		int index = gcLists.get(key).indexOf(body);
		
		// TODO why we need this?
	}
	
	public void appendGcList(WebSocket sourceSocket, String data){
		DataObject obj = new DataObject();
		obj.parseJson(data);
		
		String key = obj.getVariableKey();
		String body = obj.getBody();
		
		ArrayList<String> value = gcLists.get(key);
		value.add(body);
		
		gcLists.put(key, value);
		
		//TODO
		// sent out msg
		if(obj.getVariableAutoSync().equals("true")){
			this.broadcast(sourceSocket, data, false);
		}
	}
	
	private ArrayList<String> jsonToArray(String body){
		ArrayList<String> value = new ArrayList<String>();
		if(body.length()>=2){
			body = body.substring(1, body.length()-1);
			String[] items = body.split(",");
			for (String item: items){
				value.add(item);
			}
		}
		
		return value;
	}
	
	private String arrayToJson(ArrayList<String> value){
		String body = value.toString();
		return body;
	}
	
	public void sendGcList(WebSocket sourceSocket, String data){
		DataObject obj = new DataObject();
		obj.parseJson(data);
		
		String key = obj.getVariableKey();
		sendGcListByKey(sourceSocket, key);
	}
	
	public void sendGcListItem(WebSocket sourceSocket, String data){
		DataObject obj = new DataObject();
		obj.parseJson(data);
		
		String key = obj.getVariableKey();
		String body = obj.getBody();
		int index = Integer.parseInt(body);
		
		sendGcListItemByIndex(sourceSocket, key, index);
	}
	
	private void sendGcListItemByIndex(WebSocket sourceSocket, String key, int index){
		
		String body = gcLists.get(key).get(index);
		
		DataObject objBack = new DataObject();
		objBack.setBody(body);
		objBack.setAction("SYNC");
		objBack.setVariables(key);
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
		f.setTimeZone(TimeZone.getTimeZone("UTC"));
		String timestamp = f.format(new Date());
		objBack.setTimestamp(timestamp);
		this.broadcast(sourceSocket, objBack.getJson(), false);
	}
	
	private void sendGcListByKey(WebSocket sourceSocket, String key){
		ArrayList<String> value = null;
		if(gcLists.containsKey(key)){
			value = gcLists.get(key);
		}
		
		String body = arrayToJson(value);
		
		DataObject objBack = new DataObject();
		objBack.setBody(body);
		objBack.setAction("SYNC");
		objBack.setVariables(key);
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
		f.setTimeZone(TimeZone.getTimeZone("UTC"));
		String timestamp = f.format(new Date());
		objBack.setTimestamp(timestamp);
		this.broadcast(sourceSocket, objBack.getJson(), false);
	}
	
	public void createGcList(WebSocket sourceSocket, String data){
		DataObject obj = new DataObject();
		obj.parseJson(data);
		
		String key = obj.getVariableKey();
		
		gcLists.put(key, new ArrayList<String>());
		
		//TODO
		// sent out msg
		if(obj.getVariableAutoSync().equals("true")){
			this.broadcast(sourceSocket, data, false);
		}
	}
	
	public void sendGcObjects(WebSocket sourceSocket, String data){
		DataObject obj = new DataObject();
		obj.parseJson(data);
		
		String key = obj.getVariableKey();
		sendGcObjectsByKey(sourceSocket, key);
	}
	
	private void sendGcObjectsByKey(WebSocket sourceSocket, String key){
		String value = null;
		if(gcObjects.containsKey(key)){
			value = gcObjects.get(key);
		}
		
		
		DataObject objBack = new DataObject();
		objBack.setBody(value);
		objBack.setAction("SYNC");
		objBack.setVariables(key);
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
		f.setTimeZone(TimeZone.getTimeZone("UTC"));
		String timestamp = f.format(new Date());
		objBack.setTimestamp(timestamp);
		this.broadcast(sourceSocket, objBack.getJson(), false);
	}
	
	public void updateGcObjects(WebSocket sourceSocket, String data){
		DataObject obj = new DataObject();
		obj.parseJson(data);
		
		String key = obj.getVariableKey();
		String value = obj.getBody();
		
		gcObjects.put(key, value);
		
		if(obj.getVariableAutoSync().equals("true")){
			//broadcast
			// TODO
			// broadcast data, not the origin data
			sendGcObjectsByKey(sourceSocket, key);
		}
	}

	@Override
	public void onOpen(WebSocket arg0, ClientHandshake arg1)
	{
		
	}
	
	private void broadcast(WebSocket sourceSocket, String data, boolean excludeMode)
	{
		Collection<WebSocket> sockets = this.connections();
		for (Iterator<WebSocket> iterator = sockets.iterator(); iterator.hasNext();) 
		{
			WebSocket socket = iterator.next();
			if (socket != sourceSocket || excludeMode == false)
			{
				socket.send(data);
			}
		}
	}
	
//	private void broadcastUserList()
//	{
//		GCUserList list = new GCUserList();
//		list.setUserList(this.userList.values());
//		
//		Collection<WebSocket> sockets = this.connections();
//		for (Iterator<WebSocket> iterator = sockets.iterator(); iterator.hasNext();) 
//		{
//			WebSocket socket = iterator.next();
//			socket.send(list.getJson());
//		}
//	}
	
	private void addUser(WebSocket sourceSocket, String data)
	{
		ArrayList<String> users;
		if (this.gcLists.containsKey("USERS")){
			users = gcLists.get("USERS");
		}
		else{
			users = new ArrayList<String>();
		}
		
		DataObject obj = new DataObject();
		obj.parseJson(data);
		users.add(obj.getBody());
		gcLists.put("USERS", users);
		
		
//		if (!this.userList.containsKey(sourceSocket))
//		{
//			GCUser newUser = new GCUser();
//			this.userList.put(sourceSocket, newUser);
//		}
		
//		GCUser user = this.userList.get(sourceSocket);
//		user.parseJson(data);
		// TODO
		// may need change
		//this.updateHost();
		sendGcListByKey(sourceSocket, "USERS");
	}
	
//	private void updateHost()
//	{
//		boolean curr = true;
//		Iterator<GCUser> iterator = this.userList.values().iterator();
//		while (iterator.hasNext())
//		{
//			GCUser user = iterator.next();
//			if (curr)
//			{
//				user.setIsHost("1");
//				curr = false;
//			}
//			else
//			{
//				user.setIsHost("0");
//			}
//		}
//	}

}