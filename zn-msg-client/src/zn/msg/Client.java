package zn.msg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter.Listener;
import zn.Configuration;
import zn.ExecutorManager;
import zn.Json;
import zn.Logger;
import zn.Utils;
import zn.msg.client.CallableFn;
import zn.msg.client.Callback;
import zn.msg.client.Context;
import zn.msg.client.EventHandler;
import zn.msg.client.IdentityGetter;
import zn.msg.client.model.AnnounceAckMSG;
import zn.msg.client.model.AnnounceMSG;
import zn.msg.client.model.CallMSG;
import zn.msg.client.model.CallResult;
import zn.msg.client.model.CallReturnMSG;
import zn.msg.client.model.Identity;

public class Client 
{
  private static final Logger LOG=Logger.get(Client.class);

  private String location, ctx;
  private Identity identity;
  private Socket socket;  
  private Map<String, String> callClasses;
  private Map<String, Callback<CallReturnMSG>> calls=new HashMap<String, Callback<CallReturnMSG>>();
  private ExecutorManager eman;

  private EventHandler eventHandler;
  private IdentityGetter identityGetter;

  public Client()
  {
    callClasses=new HashMap<String, String>();
  }

  public void setExecutorManager(ExecutorManager eman)
  {
    this.eman=eman;
  }
  
  public void setIdentityGetter(IdentityGetter getter)
  {
    identityGetter=getter;
  }

  public void setEventHandler(EventHandler eh)
  {
    eventHandler=eh;
  }
  public void connect(String location, String ctx, Identity identity) throws Exception
  {
    this.location=location;
    this.ctx=ctx;
    this.identity=identity;

    Map<String, List<String>> headers=new HashMap<String, List<String>>();
    List<String> clientTypeValues=new ArrayList<String>();
    clientTypeValues.add("java-msg-client");
    headers.put("zn-msg-client-type", clientTypeValues);

    IO.Options options=IO.Options.builder()
                         .setPath(ctx)
                         .setReconnection(true)
                         .setExtraHeaders(headers)
                         .build();
    
    socket=IO.socket(location, options);
    socket.on("connect", new Listener() {public void call(Object... args) {Client.this.onConnect(args);}});
    socket.on("disconnect", new Listener() {public void call(Object... args) {Client.this.onDisconnect(args);}});
    socket.on("/zn/message", new Listener() {public void call(Object... args) {Client.this.onMessage(args);}});
    socket.on("/zn/announce/ack", new Listener() {public void call(Object... args) {Client.this.onAnnounceAck(args);}});
    socket.on("/zn/call", new Listener() {public void call(Object... args) {Client.this.onCall(args);}});
    socket.on("/zn/call/result", new Listener() {public void call(Object... args) {Client.this.onCallResult(args);}});

    socket.connect();
  }

  public void disconnect()
  {
    socket.close();
  }

  public void addCallImpl(String name, String className)
  {
    callClasses.put(name, className);
  }

  public void onConnect(Object... args)
  {
    LOG.info("connected to msg service at "+location+ctx);
    List<String> supportedCalls=new ArrayList<>();
    supportedCalls.addAll(callClasses.keySet());
    for(String name:supportedCalls) LOG.info("added call "+name);

    LOG.info("announcing to server ...");
    
    Identity identityToSend=identity;
    if(identityGetter!=null) identityToSend=identityGetter.$();

    if(identityToSend==null)
    {
      LOG.error("unable to get identity");
      return;
    }
    AnnounceMSG msg=new AnnounceMSG();
    msg.setIdentity(identityToSend);
    msg.setClientType("java-msg-client");
    msg.setCalls(supportedCalls);
    socket.emit("/zn/announce", Json.stringify(msg));
  }

  public void onDisconnect(Object... args)
  {
    LOG.info("disconnected from msg service at "+location+ctx);
    if(eventHandler!=null) eventHandler.onDisconnect(this);
  }

  public void onAnnounceAck(Object... args)
  {
    JSONObject jo=(JSONObject)args[0];
    AnnounceAckMSG msg=Json.parseJson(jo.toString(), AnnounceAckMSG.class);
    LOG.info("announce ack received, client-id="+msg.getClientId());
    if(eventHandler!=null) eventHandler.onConnect(this, msg.getClientId());
  }

  public void onCall(Object... args)
  {
    JSONObject jo=(JSONObject)args[0];
    JSONObject payload=null;
    try
    {
      payload=jo.getJSONObject("payload");
    }
    catch(Exception ex)
    {
      LOG.error(ex);
    }
    
    CallMSG msg=Json.parseJson(jo.toString(), CallMSG.class);
    msg.setPayloadJO(payload);

    LOG.info("received call for fn="+msg.getFn(), jo.toString());
    String className=callClasses.get(msg.getFn());
    if(className==null)
    {
      returnCall(msg, CallResult.error("NOT-IMPLEMENTED", "The requested call is not implemented by the client"));
      return;
    }
    
    CallableFn callableFn=null;
    try
    {
      Class<?> clazz=Class.forName(className);
      callableFn=(CallableFn)(clazz.getConstructor(new Class[]{}).newInstance(new Object[]{}));
    }
    catch(Exception ex)
    {
      LOG.error(ex);
      returnCall(msg, CallResult.error("FN-CREATION-ERROR", "Error occured while creating the function"));
      return;
    }

    LOG.info("executing call for fn="+msg.getFn(), "implementation class="+className);
    Context context=new Context(Client.this);
    callableFn.setContext(context);
    callableFn.setMSG(msg);
    eman.submit(callableFn);
  }

  public void returnCall(CallMSG callMsg, CallResult callResult)
  {
    CallReturnMSG msg=new CallReturnMSG();
    msg.setId(callMsg.getId());
    msg.setResult(callResult);
    String txt=Json.stringify(msg);
    LOG.info("sending call result", txt);
    socket.emit("/zn/call/result", txt);
  }

  
  public void call(String name, Object payload, Callback<CallReturnMSG> onResultCallback)
  {
    String id=Utils.shortid();
    CallMSG msg=new CallMSG();
    msg.setId(id);
    msg.setFn(name);
    try
    {
      msg.setPayload(payload);
    }
    catch(Exception ex)
    {
      LOG.error(ex);
    }
    calls.put(id, onResultCallback);
    socket.emit("/zn/call", Json.stringify(msg));
  }

  public void onCallResult(Object... args)
  {
    JSONObject jo=(JSONObject)args[0];
    JSONObject result=null;
    try
    {
      result=jo.getJSONObject("result");
    }
    catch(Exception ex)
    {
      LOG.error(ex);
    }
    
    CallReturnMSG msg=Json.parseJson(jo.toString(), CallReturnMSG.class);
    msg.setResultJO(result);

    String id=msg.getId();
    LOG.info("call result received, id="+id, jo.toString());

    Callback<CallReturnMSG> onResultCallback=calls.get(id);
    eman.submit(()->onResultCallback.$(msg));
  }

  public void onMessage(Object... args)
  {
    if(eventHandler!=null) eventHandler.onMessage(this, (JSONObject)args[0]);
  }

}
