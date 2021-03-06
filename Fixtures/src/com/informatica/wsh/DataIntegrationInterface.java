package com.informatica.wsh;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.jws.soap.SOAPBinding.Style;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.Action;
import javax.xml.ws.FaultAction;
import javax.xml.ws.Holder;
// !DO NOT EDIT THIS FILE!
// This source file is generated by Oracle tools
// Contents may be subject to change
// For reporting problems, use the following
// Version = Oracle WebServices (11.1.1.0.0, build 101221.1153.15811)

@WebService(wsdlLocation="http://localhost:7333/wsh/services/BatchServices/DataIntegration?WSDL",
  targetNamespace="http://www.informatica.com/wsh", name="DataIntegrationInterface")
@XmlSeeAlso(
  { com.informatica.wsh.ObjectFactory.class })
@SOAPBinding(style=Style.DOCUMENT, parameterStyle=ParameterStyle.BARE)
public interface DataIntegrationInterface
{
  @WebMethod
  @SOAPBinding(parameterStyle=ParameterStyle.BARE)
  @Action(input="http://www.informatica.com/wsh/DataIntegrationInterface/LoginRequest", fault =
      { @FaultAction(value="http://www.informatica.com/wsh/DataIntegrationInterface/login/Fault/Fault",
          className=com.informatica.wsh.Fault.class) }, output="http://www.informatica.com/wsh/DataIntegrationInterface/LoginResponse")
  @WebResult(targetNamespace="http://www.informatica.com/wsh", partName="param",
    name="LoginReturn")
  public String login(@WebParam(targetNamespace="http://www.informatica.com/wsh",
      partName="param", name="Login")
    com.informatica.wsh.LoginRequest param, @WebParam(targetNamespace="http://www.informatica.com/wsh",
      partName="Context", name="Context", header=true, mode=Mode.OUT)
    Holder<com.informatica.wsh.SessionHeader> Context)
    throws com.informatica.wsh.Fault;

  @WebMethod
  @SOAPBinding(parameterStyle=ParameterStyle.BARE)
  @Action(input="http://www.informatica.com/wsh/DataIntegrationInterface/LogoutRequest", fault =
      { @FaultAction(value="http://www.informatica.com/wsh/DataIntegrationInterface/logout/Fault/Fault",
          className=com.informatica.wsh.Fault.class) }, output="http://www.informatica.com/wsh/DataIntegrationInterface/LogoutResponse")
  @WebResult(targetNamespace="http://www.informatica.com/wsh", partName="param",
    name="LogoutReturn")
  public com.informatica.wsh.VoidResponse logout(@WebParam(targetNamespace="http://www.informatica.com/wsh",
      partName="param", name="Logout")
    com.informatica.wsh.VoidRequest param, @WebParam(targetNamespace="http://www.informatica.com/wsh",
      partName="Context", name="Context", header=true)
    com.informatica.wsh.SessionHeader Context)
    throws com.informatica.wsh.Fault;

  @WebMethod
  @SOAPBinding(parameterStyle=ParameterStyle.BARE)
  @Action(input="http://www.informatica.com/wsh/DataIntegrationInterface/InitializeDIServerConnectionRequest", fault =
      { @FaultAction(value="http://www.informatica.com/wsh/DataIntegrationInterface/initializeDIServerConnection/Fault/Fault",
          className=com.informatica.wsh.Fault.class) }, output="http://www.informatica.com/wsh/DataIntegrationInterface/InitializeDIServerConnectionResponse")
  @WebResult(targetNamespace="http://www.informatica.com/wsh", partName="param",
    name="InitializeDIServerConnectionReturn")
  public com.informatica.wsh.VoidResponse initializeDIServerConnection(@WebParam(targetNamespace="http://www.informatica.com/wsh",
      partName="param", name="InitializeDIServerConnection")
    com.informatica.wsh.InitializeDIServerConnectionRequest param,
    @WebParam(targetNamespace="http://www.informatica.com/wsh", partName="Context",
      name="Context", header=true)
    com.informatica.wsh.SessionHeader Context)
    throws com.informatica.wsh.Fault;

  @WebMethod
  @SOAPBinding(parameterStyle=ParameterStyle.BARE)
  @Action(input="http://www.informatica.com/wsh/DataIntegrationInterface/DeinitializeDIServerConnectionRequest", fault =
      { @FaultAction(value="http://www.informatica.com/wsh/DataIntegrationInterface/deinitializeDIServerConnection/Fault/Fault",
          className=com.informatica.wsh.Fault.class) }, output="http://www.informatica.com/wsh/DataIntegrationInterface/DeinitializeDIServerConnectionResponse")
  @WebResult(targetNamespace="http://www.informatica.com/wsh", partName="param",
    name="DeinitializeDIServerConnectionReturn")
  public com.informatica.wsh.VoidResponse deinitializeDIServerConnection(@WebParam(targetNamespace="http://www.informatica.com/wsh",
      partName="param", name="DeinitializeDIServerConnection")
    com.informatica.wsh.VoidRequest param, @WebParam(targetNamespace="http://www.informatica.com/wsh",
      partName="Context", name="Context", header=true)
    com.informatica.wsh.SessionHeader Context)
    throws com.informatica.wsh.Fault;

  @WebMethod
  @SOAPBinding(parameterStyle=ParameterStyle.BARE)
  @Action(input="http://www.informatica.com/wsh/DataIntegrationInterface/StartWorkflowLogFetchRequest", fault =
      { @FaultAction(value="http://www.informatica.com/wsh/DataIntegrationInterface/startWorkflowLogFetch/Fault/Fault",
          className=com.informatica.wsh.Fault.class) }, output="http://www.informatica.com/wsh/DataIntegrationInterface/StartWorkflowLogFetchResponse")
  @WebResult(targetNamespace="http://www.informatica.com/wsh", partName="param",
    name="StartWorkflowLogFetchReturn")
  public int startWorkflowLogFetch(@WebParam(targetNamespace="http://www.informatica.com/wsh",
      partName="param", name="StartWorkflowLogFetch")
    com.informatica.wsh.StartWorkflowLogFetchRequest param,
    @WebParam(targetNamespace="http://www.informatica.com/wsh", partName="Context",
      name="Context", header=true)
    com.informatica.wsh.SessionHeader Context)
    throws com.informatica.wsh.Fault;

  @WebMethod
  @SOAPBinding(parameterStyle=ParameterStyle.BARE)
  @Action(input="http://www.informatica.com/wsh/DataIntegrationInterface/StartSessionLogFetchRequest", fault =
      { @FaultAction(value="http://www.informatica.com/wsh/DataIntegrationInterface/startSessionLogFetch/Fault/Fault",
          className=com.informatica.wsh.Fault.class) }, output="http://www.informatica.com/wsh/DataIntegrationInterface/StartSessionLogFetchResponse")
  @WebResult(targetNamespace="http://www.informatica.com/wsh", partName="param",
    name="StartSessionLogFetchReturn")
  public int startSessionLogFetch(@WebParam(targetNamespace="http://www.informatica.com/wsh",
      partName="param", name="StartSessionLogFetch")
    com.informatica.wsh.StartSessionLogFetchRequest param,
    @WebParam(targetNamespace="http://www.informatica.com/wsh", partName="Context",
      name="Context", header=true)
    com.informatica.wsh.SessionHeader Context)
    throws com.informatica.wsh.Fault;

  @WebMethod
  @SOAPBinding(parameterStyle=ParameterStyle.BARE)
  @Action(input="http://www.informatica.com/wsh/DataIntegrationInterface/GetNextLogSegmentRequest", fault =
      { @FaultAction(value="http://www.informatica.com/wsh/DataIntegrationInterface/getNextLogSegment/Fault/Fault",
          className=com.informatica.wsh.Fault.class) }, output="http://www.informatica.com/wsh/DataIntegrationInterface/GetNextLogSegmentResponse")
  @WebResult(targetNamespace="http://www.informatica.com/wsh", partName="param",
    name="GetNextLogSegmentReturn")
  public com.informatica.wsh.LogSegment getNextLogSegment(@WebParam(targetNamespace="http://www.informatica.com/wsh",
      partName="param", name="GetNextLogSegment")
    com.informatica.wsh.GetNextLogSegmentRequest param,
    @WebParam(targetNamespace="http://www.informatica.com/wsh", partName="Context",
      name="Context", header=true)
    com.informatica.wsh.SessionHeader Context)
    throws com.informatica.wsh.Fault;

  @WebMethod
  @SOAPBinding(parameterStyle=ParameterStyle.BARE)
  @Action(input="http://www.informatica.com/wsh/DataIntegrationInterface/pingDIServerRequest", fault =
      { @FaultAction(value="http://www.informatica.com/wsh/DataIntegrationInterface/pingDIServer/Fault/Fault",
          className=com.informatica.wsh.Fault.class) }, output="http://www.informatica.com/wsh/DataIntegrationInterface/pingDIServerResponse")
  @WebResult(targetNamespace="http://www.informatica.com/wsh", partName="PingDIServerReturn",
    name="PingDIServerReturn")
  public com.informatica.wsh.EPingState pingDIServer(@WebParam(targetNamespace="http://www.informatica.com/wsh",
      partName="PingDIServer", name="PingDIServer")
    com.informatica.wsh.PingDIServerRequest PingDIServer,
    @WebParam(targetNamespace="http://www.informatica.com/wsh", partName="Context",
      name="Context", header=true)
    com.informatica.wsh.SessionHeader Context)
    throws com.informatica.wsh.Fault;

  @WebMethod
  @SOAPBinding(parameterStyle=ParameterStyle.BARE)
  @Action(input="http://www.informatica.com/wsh/DataIntegrationInterface/getDIServerPropertiesRequest", fault =
      { @FaultAction(value="http://www.informatica.com/wsh/DataIntegrationInterface/getDIServerProperties/Fault/Fault",
          className=com.informatica.wsh.Fault.class) }, output="http://www.informatica.com/wsh/DataIntegrationInterface/getDIServerPropertiesResponse")
  @WebResult(targetNamespace="http://www.informatica.com/wsh", partName="GetDIServerPropertiesReturn",
    name="GetDIServerPropertiesReturn")
  public com.informatica.wsh.DIServerProperties getDIServerProperties(@WebParam(targetNamespace="http://www.informatica.com/wsh",
      partName="GetDIServerProperties", name="GetDIServerProperties")
    com.informatica.wsh.DIServiceInfo GetDIServerProperties,
    @WebParam(targetNamespace="http://www.informatica.com/wsh", partName="Context",
      name="Context", header=true)
    com.informatica.wsh.SessionHeader Context)
    throws com.informatica.wsh.Fault;

  @WebMethod
  @SOAPBinding(parameterStyle=ParameterStyle.BARE)
  @Action(input="http://www.informatica.com/wsh/DataIntegrationInterface/startWorkflowRequest", fault =
      { @FaultAction(value="http://www.informatica.com/wsh/DataIntegrationInterface/startWorkflow/Fault/Fault",
          className=com.informatica.wsh.Fault.class) }, output="http://www.informatica.com/wsh/DataIntegrationInterface/startWorkflowResponse")
  @WebResult(targetNamespace="http://www.informatica.com/wsh", partName="StartWorkflowReturn",
    name="StartWorkflowReturn")
  public com.informatica.wsh.VoidResponse startWorkflow(@WebParam(targetNamespace="http://www.informatica.com/wsh",
      partName="StartWorkflow", name="StartWorkflow")
    com.informatica.wsh.WorkflowRequest StartWorkflow,
    @WebParam(targetNamespace="http://www.informatica.com/wsh", partName="Context",
      name="Context", header=true)
    com.informatica.wsh.SessionHeader Context)
    throws com.informatica.wsh.Fault;

  @WebMethod
  @SOAPBinding(parameterStyle=ParameterStyle.BARE)
  @Action(input="http://www.informatica.com/wsh/DataIntegrationInterface/startWorkflowExRequest", fault =
      { @FaultAction(value="http://www.informatica.com/wsh/DataIntegrationInterface/startWorkflowEx/Fault/Fault",
          className=com.informatica.wsh.Fault.class) }, output="http://www.informatica.com/wsh/DataIntegrationInterface/startWorkflowExResponse")
  @WebResult(targetNamespace="http://www.informatica.com/wsh", partName="StartWorkflowExReturn",
    name="StartWorkflowExReturn")
  public com.informatica.wsh.TypeStartWorkflowExResponse startWorkflowEx(@WebParam(targetNamespace="http://www.informatica.com/wsh",
      partName="StartWorkflowEx", name="StartWorkflowEx")
    com.informatica.wsh.TypeStartWorkflowExRequest StartWorkflowEx,
    @WebParam(targetNamespace="http://www.informatica.com/wsh", partName="Context",
      name="Context", header=true)
    com.informatica.wsh.SessionHeader Context)
    throws com.informatica.wsh.Fault;

  @WebMethod
  @SOAPBinding(parameterStyle=ParameterStyle.BARE)
  @Action(input="http://www.informatica.com/wsh/DataIntegrationInterface/startWorkflowFromTaskRequest", fault =
      { @FaultAction(value="http://www.informatica.com/wsh/DataIntegrationInterface/startWorkflowFromTask/Fault/Fault",
          className=com.informatica.wsh.Fault.class) }, output="http://www.informatica.com/wsh/DataIntegrationInterface/startWorkflowFromTaskResponse")
  @WebResult(targetNamespace="http://www.informatica.com/wsh", partName="StartWorkflowFromTaskReturn",
    name="StartWorkflowFromTaskReturn")
  public com.informatica.wsh.VoidResponse startWorkflowFromTask(@WebParam(targetNamespace="http://www.informatica.com/wsh",
      partName="StartWorkflowFromTask", name="StartWorkflowFromTask")
    com.informatica.wsh.WorkflowRequest StartWorkflowFromTask,
    @WebParam(targetNamespace="http://www.informatica.com/wsh", partName="Context",
      name="Context", header=true)
    com.informatica.wsh.SessionHeader Context)
    throws com.informatica.wsh.Fault;

  @WebMethod
  @SOAPBinding(parameterStyle=ParameterStyle.BARE)
  @Action(input="http://www.informatica.com/wsh/DataIntegrationInterface/stopWorkflowRequest", fault =
      { @FaultAction(value="http://www.informatica.com/wsh/DataIntegrationInterface/stopWorkflow/Fault/Fault",
          className=com.informatica.wsh.Fault.class) }, output="http://www.informatica.com/wsh/DataIntegrationInterface/stopWorkflowResponse")
  @WebResult(targetNamespace="http://www.informatica.com/wsh", partName="StopWorkflowReturn",
    name="StopWorkflowReturn")
  public com.informatica.wsh.VoidResponse stopWorkflow(@WebParam(targetNamespace="http://www.informatica.com/wsh",
      partName="StopWorkflow", name="StopWorkflow")
    com.informatica.wsh.WorkflowRequest StopWorkflow,
    @WebParam(targetNamespace="http://www.informatica.com/wsh", partName="Context",
      name="Context", header=true)
    com.informatica.wsh.SessionHeader Context)
    throws com.informatica.wsh.Fault;

  @WebMethod
  @SOAPBinding(parameterStyle=ParameterStyle.BARE)
  @Action(input="http://www.informatica.com/wsh/DataIntegrationInterface/scheduleWorkflowRequest", fault =
      { @FaultAction(value="http://www.informatica.com/wsh/DataIntegrationInterface/scheduleWorkflow/Fault/Fault",
          className=com.informatica.wsh.Fault.class) }, output="http://www.informatica.com/wsh/DataIntegrationInterface/scheduleWorkflowResponse")
  @WebResult(targetNamespace="http://www.informatica.com/wsh", partName="ScheduleWorkflowReturn",
    name="ScheduleWorkflowReturn")
  public com.informatica.wsh.VoidResponse scheduleWorkflow(@WebParam(targetNamespace="http://www.informatica.com/wsh",
      partName="ScheduleWorkflow", name="ScheduleWorkflow")
    com.informatica.wsh.WorkflowRequest ScheduleWorkflow,
    @WebParam(targetNamespace="http://www.informatica.com/wsh", partName="Context",
      name="Context", header=true)
    com.informatica.wsh.SessionHeader Context)
    throws com.informatica.wsh.Fault;

  @WebMethod
  @SOAPBinding(parameterStyle=ParameterStyle.BARE)
  @Action(input="http://www.informatica.com/wsh/DataIntegrationInterface/unscheduleWorkflowRequest", fault =
      { @FaultAction(value="http://www.informatica.com/wsh/DataIntegrationInterface/unscheduleWorkflow/Fault/Fault",
          className=com.informatica.wsh.Fault.class) }, output="http://www.informatica.com/wsh/DataIntegrationInterface/unscheduleWorkflowResponse")
  @WebResult(targetNamespace="http://www.informatica.com/wsh", partName="UnscheduleWorkflowReturn",
    name="UnscheduleWorkflowReturn")
  public com.informatica.wsh.VoidResponse unscheduleWorkflow(@WebParam(targetNamespace="http://www.informatica.com/wsh",
      partName="UnscheduleWorkflow", name="UnscheduleWorkflow")
    com.informatica.wsh.WorkflowRequest UnscheduleWorkflow,
    @WebParam(targetNamespace="http://www.informatica.com/wsh", partName="Context",
      name="Context", header=true)
    com.informatica.wsh.SessionHeader Context)
    throws com.informatica.wsh.Fault;

  @WebMethod
  @SOAPBinding(parameterStyle=ParameterStyle.BARE)
  @Action(input="http://www.informatica.com/wsh/DataIntegrationInterface/waitTillWorkflowCompleteRequest", fault =
      { @FaultAction(value="http://www.informatica.com/wsh/DataIntegrationInterface/waitTillWorkflowComplete/Fault/Fault",
          className=com.informatica.wsh.Fault.class) }, output="http://www.informatica.com/wsh/DataIntegrationInterface/waitTillWorkflowCompleteResponse")
  @WebResult(targetNamespace="http://www.informatica.com/wsh", partName="WaitTillWorkflowCompleteReturn",
    name="WaitTillWorkflowCompleteReturn")
  public com.informatica.wsh.VoidResponse waitTillWorkflowComplete(@WebParam(targetNamespace="http://www.informatica.com/wsh",
      partName="WaitTillWorkflowComplete", name="WaitTillWorkflowComplete")
    com.informatica.wsh.WorkflowRequest WaitTillWorkflowComplete,
    @WebParam(targetNamespace="http://www.informatica.com/wsh", partName="Context",
      name="Context", header=true)
    com.informatica.wsh.SessionHeader Context)
    throws com.informatica.wsh.Fault;

  @WebMethod
  @SOAPBinding(parameterStyle=ParameterStyle.BARE)
  @Action(input="http://www.informatica.com/wsh/DataIntegrationInterface/resumeWorkflowRequest", fault =
      { @FaultAction(value="http://www.informatica.com/wsh/DataIntegrationInterface/resumeWorkflow/Fault/Fault",
          className=com.informatica.wsh.Fault.class) }, output="http://www.informatica.com/wsh/DataIntegrationInterface/resumeWorkflowResponse")
  @WebResult(targetNamespace="http://www.informatica.com/wsh", partName="ResumeWorkflowReturn",
    name="ResumeWorkflowReturn")
  public com.informatica.wsh.VoidResponse resumeWorkflow(@WebParam(targetNamespace="http://www.informatica.com/wsh",
      partName="ResumeWorkflow", name="ResumeWorkflow")
    com.informatica.wsh.WorkflowRequest ResumeWorkflow,
    @WebParam(targetNamespace="http://www.informatica.com/wsh", partName="Context",
      name="Context", header=true)
    com.informatica.wsh.SessionHeader Context)
    throws com.informatica.wsh.Fault;

  @WebMethod
  @SOAPBinding(parameterStyle=ParameterStyle.BARE)
  @Action(input="http://www.informatica.com/wsh/DataIntegrationInterface/startTaskRequest", fault =
      { @FaultAction(value="http://www.informatica.com/wsh/DataIntegrationInterface/startTask/Fault/Fault",
          className=com.informatica.wsh.Fault.class) }, output="http://www.informatica.com/wsh/DataIntegrationInterface/startTaskResponse")
  @WebResult(targetNamespace="http://www.informatica.com/wsh", partName="StartTaskReturn",
    name="StartTaskReturn")
  public com.informatica.wsh.VoidResponse startTask(@WebParam(targetNamespace="http://www.informatica.com/wsh",
      partName="StartTask", name="StartTask")
    com.informatica.wsh.TaskRequest StartTask, @WebParam(targetNamespace="http://www.informatica.com/wsh",
      partName="Context", name="Context", header=true)
    com.informatica.wsh.SessionHeader Context)
    throws com.informatica.wsh.Fault;

  @WebMethod
  @SOAPBinding(parameterStyle=ParameterStyle.BARE)
  @Action(input="http://www.informatica.com/wsh/DataIntegrationInterface/stopTaskRequest", fault =
      { @FaultAction(value="http://www.informatica.com/wsh/DataIntegrationInterface/stopTask/Fault/Fault",
          className=com.informatica.wsh.Fault.class) }, output="http://www.informatica.com/wsh/DataIntegrationInterface/stopTaskResponse")
  @WebResult(targetNamespace="http://www.informatica.com/wsh", partName="StopTaskReturn",
    name="StopTaskReturn")
  public com.informatica.wsh.VoidResponse stopTask(@WebParam(targetNamespace="http://www.informatica.com/wsh",
      partName="StopTask", name="StopTask")
    com.informatica.wsh.TaskRequest StopTask, @WebParam(targetNamespace="http://www.informatica.com/wsh",
      partName="Context", name="Context", header=true)
    com.informatica.wsh.SessionHeader Context)
    throws com.informatica.wsh.Fault;

  @WebMethod
  @SOAPBinding(parameterStyle=ParameterStyle.BARE)
  @Action(input="http://www.informatica.com/wsh/DataIntegrationInterface/waitTillTaskCompleteRequest", fault =
      { @FaultAction(value="http://www.informatica.com/wsh/DataIntegrationInterface/waitTillTaskComplete/Fault/Fault",
          className=com.informatica.wsh.Fault.class) }, output="http://www.informatica.com/wsh/DataIntegrationInterface/waitTillTaskCompleteResponse")
  @WebResult(targetNamespace="http://www.informatica.com/wsh", partName="WaitTillTaskCompleteReturn",
    name="WaitTillTaskCompleteReturn")
  public com.informatica.wsh.VoidResponse waitTillTaskComplete(@WebParam(targetNamespace="http://www.informatica.com/wsh",
      partName="WaitTillTaskComplete", name="WaitTillTaskComplete")
    com.informatica.wsh.TaskRequest WaitTillTaskComplete,
    @WebParam(targetNamespace="http://www.informatica.com/wsh", partName="Context",
      name="Context", header=true)
    com.informatica.wsh.SessionHeader Context)
    throws com.informatica.wsh.Fault;

  @WebMethod
  @SOAPBinding(parameterStyle=ParameterStyle.BARE)
  @Action(input="http://www.informatica.com/wsh/DataIntegrationInterface/recoverWorkflowRequest", fault =
      { @FaultAction(value="http://www.informatica.com/wsh/DataIntegrationInterface/recoverWorkflow/Fault/Fault",
          className=com.informatica.wsh.Fault.class) }, output="http://www.informatica.com/wsh/DataIntegrationInterface/recoverWorkflowResponse")
  @WebResult(targetNamespace="http://www.informatica.com/wsh", partName="RecoverWorkflowReturn",
    name="RecoverWorkflowReturn")
  public com.informatica.wsh.VoidResponse recoverWorkflow(@WebParam(targetNamespace="http://www.informatica.com/wsh",
      partName="RecoverWorkflow", name="RecoverWorkflow")
    com.informatica.wsh.WorkflowRequest RecoverWorkflow,
    @WebParam(targetNamespace="http://www.informatica.com/wsh", partName="Context",
      name="Context", header=true)
    com.informatica.wsh.SessionHeader Context)
    throws com.informatica.wsh.Fault;

  @WebMethod
  @SOAPBinding(parameterStyle=ParameterStyle.BARE)
  @Action(input="http://www.informatica.com/wsh/DataIntegrationInterface/monitorDIServerRequest", fault =
      { @FaultAction(value="http://www.informatica.com/wsh/DataIntegrationInterface/monitorDIServer/Fault/Fault",
          className=com.informatica.wsh.Fault.class) }, output="http://www.informatica.com/wsh/DataIntegrationInterface/monitorDIServerResponse")
  @WebResult(targetNamespace="http://www.informatica.com/wsh", partName="MonitorDIServerReturn",
    name="MonitorDIServerReturn")
  public com.informatica.wsh.DIServerDetails monitorDIServer(@WebParam(targetNamespace="http://www.informatica.com/wsh",
      partName="MonitorDIServer", name="MonitorDIServer")
    com.informatica.wsh.MonitorDIServerRequest MonitorDIServer,
    @WebParam(targetNamespace="http://www.informatica.com/wsh", partName="Context",
      name="Context", header=true)
    com.informatica.wsh.SessionHeader Context)
    throws com.informatica.wsh.Fault;

  @WebMethod
  @SOAPBinding(parameterStyle=ParameterStyle.BARE)
  @Action(input="http://www.informatica.com/wsh/DataIntegrationInterface/getWorkflowDetailsRequest", fault =
      { @FaultAction(value="http://www.informatica.com/wsh/DataIntegrationInterface/getWorkflowDetails/Fault/Fault",
          className=com.informatica.wsh.Fault.class) }, output="http://www.informatica.com/wsh/DataIntegrationInterface/getWorkflowDetailsResponse")
  @WebResult(targetNamespace="http://www.informatica.com/wsh", partName="GetWorkflowDetailsReturn",
    name="GetWorkflowDetailsReturn")
  public com.informatica.wsh.WorkflowDetails getWorkflowDetails(@WebParam(targetNamespace="http://www.informatica.com/wsh",
      partName="GetWorkflowDetails", name="GetWorkflowDetails")
    com.informatica.wsh.WorkflowRequest GetWorkflowDetails,
    @WebParam(targetNamespace="http://www.informatica.com/wsh", partName="Context",
      name="Context", header=true)
    com.informatica.wsh.SessionHeader Context)
    throws com.informatica.wsh.Fault;

  @WebMethod
  @SOAPBinding(parameterStyle=ParameterStyle.BARE)
  @Action(input="http://www.informatica.com/wsh/DataIntegrationInterface/getWorkflowDetailsExRequest", fault =
      { @FaultAction(value="http://www.informatica.com/wsh/DataIntegrationInterface/getWorkflowDetailsEx/Fault/Fault",
          className=com.informatica.wsh.Fault.class) }, output="http://www.informatica.com/wsh/DataIntegrationInterface/getWorkflowDetailsExResponse")
  @WebResult(targetNamespace="http://www.informatica.com/wsh", partName="GetWorkflowDetailsExReturn",
    name="GetWorkflowDetailsExReturn")
  public com.informatica.wsh.DIServerDetails getWorkflowDetailsEx(@WebParam(targetNamespace="http://www.informatica.com/wsh",
      partName="GetWorkflowDetailsEx", name="GetWorkflowDetailsEx")
    com.informatica.wsh.TypeGetWorkflowDetailsExRequest GetWorkflowDetailsEx,
    @WebParam(targetNamespace="http://www.informatica.com/wsh", partName="Context",
      name="Context", header=true)
    com.informatica.wsh.SessionHeader Context)
    throws com.informatica.wsh.Fault;

  @WebMethod
  @SOAPBinding(parameterStyle=ParameterStyle.BARE)
  @Action(input="http://www.informatica.com/wsh/DataIntegrationInterface/getTaskDetailsRequest", fault =
      { @FaultAction(value="http://www.informatica.com/wsh/DataIntegrationInterface/getTaskDetails/Fault/Fault",
          className=com.informatica.wsh.Fault.class) }, output="http://www.informatica.com/wsh/DataIntegrationInterface/getTaskDetailsResponse")
  @WebResult(targetNamespace="http://www.informatica.com/wsh", partName="GetTaskDetailsReturn",
    name="GetTaskDetailsReturn")
  public com.informatica.wsh.TaskDetails getTaskDetails(@WebParam(targetNamespace="http://www.informatica.com/wsh",
      partName="GetTaskDetails", name="GetTaskDetails")
    com.informatica.wsh.TaskRequest GetTaskDetails,
    @WebParam(targetNamespace="http://www.informatica.com/wsh", partName="Context",
      name="Context", header=true)
    com.informatica.wsh.SessionHeader Context)
    throws com.informatica.wsh.Fault;

  @WebMethod
  @SOAPBinding(parameterStyle=ParameterStyle.BARE)
  @Action(input="http://www.informatica.com/wsh/DataIntegrationInterface/getTaskDetailsExRequest", fault =
      { @FaultAction(value="http://www.informatica.com/wsh/DataIntegrationInterface/getTaskDetailsEx/Fault/Fault",
          className=com.informatica.wsh.Fault.class) }, output="http://www.informatica.com/wsh/DataIntegrationInterface/getTaskDetailsExResponse")
  @WebResult(targetNamespace="http://www.informatica.com/wsh", partName="GetTaskDetailsExReturn",
    name="GetTaskDetailsExReturn")
  public com.informatica.wsh.DIServerDetails getTaskDetailsEx(@WebParam(targetNamespace="http://www.informatica.com/wsh",
      partName="GetTaskDetailsEx", name="GetTaskDetailsEx")
    com.informatica.wsh.TypeGetTaskDetailsExRequest GetTaskDetailsEx,
    @WebParam(targetNamespace="http://www.informatica.com/wsh", partName="Context",
      name="Context", header=true)
    com.informatica.wsh.SessionHeader Context)
    throws com.informatica.wsh.Fault;

  @WebMethod
  @SOAPBinding(parameterStyle=ParameterStyle.BARE)
  @Action(input="http://www.informatica.com/wsh/DataIntegrationInterface/getSessionStatisticsRequest", fault =
      { @FaultAction(value="http://www.informatica.com/wsh/DataIntegrationInterface/getSessionStatistics/Fault/Fault",
          className=com.informatica.wsh.Fault.class) }, output="http://www.informatica.com/wsh/DataIntegrationInterface/getSessionStatisticsResponse")
  @WebResult(targetNamespace="http://www.informatica.com/wsh", partName="GetSessionStatisticsReturn",
    name="GetSessionStatisticsReturn")
  public com.informatica.wsh.SessionStatistics getSessionStatistics(@WebParam(targetNamespace="http://www.informatica.com/wsh",
      partName="GetSessionStatistics", name="GetSessionStatistics")
    com.informatica.wsh.GetSessionStatisticsRequest GetSessionStatistics,
    @WebParam(targetNamespace="http://www.informatica.com/wsh", partName="Context",
      name="Context", header=true)
    com.informatica.wsh.SessionHeader Context)
    throws com.informatica.wsh.Fault;

  @WebMethod
  @SOAPBinding(parameterStyle=ParameterStyle.BARE)
  @Action(input="http://www.informatica.com/wsh/DataIntegrationInterface/getSessionPerformanceDataRequest", fault =
      { @FaultAction(value="http://www.informatica.com/wsh/DataIntegrationInterface/getSessionPerformanceData/Fault/Fault",
          className=com.informatica.wsh.Fault.class) }, output="http://www.informatica.com/wsh/DataIntegrationInterface/getSessionPerformanceDataResponse")
  @WebResult(targetNamespace="http://www.informatica.com/wsh", partName="GetSessionPerformanceDataReturn",
    name="GetSessionPerformanceDataReturn")
  public com.informatica.wsh.SessionPerformanceData getSessionPerformanceData(@WebParam(targetNamespace="http://www.informatica.com/wsh",
      partName="GetSessionPerformanceData", name="GetSessionPerformanceData")
    com.informatica.wsh.GetSessionPerformanceDataRequest GetSessionPerformanceData,
    @WebParam(targetNamespace="http://www.informatica.com/wsh", partName="Context",
      name="Context", header=true)
    com.informatica.wsh.SessionHeader Context)
    throws com.informatica.wsh.Fault;

  @WebMethod
  @SOAPBinding(parameterStyle=ParameterStyle.BARE)
  @Action(input="http://www.informatica.com/wsh/DataIntegrationInterface/getWorkflowLogRequest", fault =
      { @FaultAction(value="http://www.informatica.com/wsh/DataIntegrationInterface/getWorkflowLog/Fault/Fault",
          className=com.informatica.wsh.Fault.class) }, output="http://www.informatica.com/wsh/DataIntegrationInterface/getWorkflowLogResponse")
  @WebResult(targetNamespace="http://www.informatica.com/wsh", partName="GetWorkflowLogReturn",
    name="GetWorkflowLogReturn")
  public com.informatica.wsh.Log getWorkflowLog(@WebParam(targetNamespace="http://www.informatica.com/wsh",
      partName="GetWorkflowLog", name="GetWorkflowLog")
    com.informatica.wsh.GetWorkflowLogRequest GetWorkflowLog,
    @WebParam(targetNamespace="http://www.informatica.com/wsh", partName="Context",
      name="Context", header=true)
    com.informatica.wsh.SessionHeader Context)
    throws com.informatica.wsh.Fault;

  @WebMethod
  @SOAPBinding(parameterStyle=ParameterStyle.BARE)
  @Action(input="http://www.informatica.com/wsh/DataIntegrationInterface/getSessionLogRequest", fault =
      { @FaultAction(value="http://www.informatica.com/wsh/DataIntegrationInterface/getSessionLog/Fault/Fault",
          className=com.informatica.wsh.Fault.class) }, output="http://www.informatica.com/wsh/DataIntegrationInterface/getSessionLogResponse")
  @WebResult(targetNamespace="http://www.informatica.com/wsh", partName="GetSessoinLogReturn",
    name="GetSessionLogReturn")
  public com.informatica.wsh.Log getSessionLog(@WebParam(targetNamespace="http://www.informatica.com/wsh",
      partName="GetSessionLog", name="GetSessionLog")
    com.informatica.wsh.GetSessionLogRequest GetSessionLog,
    @WebParam(targetNamespace="http://www.informatica.com/wsh", partName="Context",
      name="Context", header=true)
    com.informatica.wsh.SessionHeader Context)
    throws com.informatica.wsh.Fault;
}
