package com.informatica.wsh;

import javax.xml.ws.WebServiceRef;
// !THE CHANGES MADE TO THIS FILE WILL BE DESTROYED IF REGENERATED!
// This source file is generated by Oracle tools
// Contents may be subject to change
// For reporting problems, use the following
// Version = Oracle WebServices (11.1.1.0.0, build 101221.1153.15811)

public class MetadataClient
{
  @WebServiceRef
  private static MetadataService metadataService;

  public static void main(String [] args)
  {
    metadataService = new MetadataService();
    MetadataInterface metadataInterface = metadataService.getMetadata();
    // Add your code to call the desired methods.
  }
}