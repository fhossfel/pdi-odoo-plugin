/*
 *   This software is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published
 *   by the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This software is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this software.  If not, see <http://www.gnu.org/licenses/>.
 *
 *   Copyright 2011 De Bortoli Wines Pty Limited (Australia)
 *   Copyright 2017 West IT Solutions
 */

package org.pentaho.di.odoo.core;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.xmlrpc.XmlRpcException;
import org.pentaho.di.core.database.DatabaseFactoryInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

import com.debortoliwines.odoo.api.FilterCollection;
import com.debortoliwines.odoo.api.ObjectAdapter;
import com.debortoliwines.odoo.api.OdooCommand;
import com.debortoliwines.odoo.api.OdooApiException;
import com.debortoliwines.odoo.api.Session;
import com.debortoliwines.odoo.api.Field;
import com.debortoliwines.odoo.api.FieldCollection;
import com.debortoliwines.odoo.api.RowCollection;
import com.debortoliwines.odoo.api.Field.FieldType;
import com.debortoliwines.odoo.api.OdooXmlRpcProxy.RPCProtocol;

/**
 * Helper class to keep common functionality in one class
 *
 * @author Pieter van der Merwe
 */
public class OdooHelper implements DatabaseFactoryInterface {

  private Session odooConnection;
  private OdooCommand commands;
  private boolean importReturnsIDS = false;

  @Override
  public String getConnectionTestReport( DatabaseMeta databaseMeta ) {

    try {
      OdooHelper helper = new OdooHelper( databaseMeta );
      helper.StartSession();

      return "Successfully connected to [" + databaseMeta.environmentSubstitute( databaseMeta.getName() ) + "]";
    } catch ( NumberFormatException e ) {
      return "Invalid port number: " + e.getMessage();
    } catch ( Exception e ) {
      return "Connection failed: " + e.getMessage();
    }
  }

  // / Need a default constructor for the "Test" button to work on the connect database dialog
  public OdooHelper() {
  }

  public OdooHelper( DatabaseMeta databaseMeta ) {
    final int port = Integer.parseInt( databaseMeta.environmentSubstitute( databaseMeta.getDatabasePortNumberString() ) );
    final RPCProtocol protocol = (port == 443) ? RPCProtocol.RPC_HTTPS : RPCProtocol.RPC_HTTP;
    odooConnection =
        new Session( protocol, databaseMeta.environmentSubstitute( databaseMeta.getHostname() ), port,
            databaseMeta.environmentSubstitute( databaseMeta.getDatabaseName() ), databaseMeta.environmentSubstitute(
            databaseMeta.getUsername() ), databaseMeta.environmentSubstitute( databaseMeta.getPassword() ) );
  }

  public void StartSession() throws Exception {
    odooConnection.startSession();

    commands = new OdooCommand(odooConnection);

    // Don't automatically filter out active items in any steps
    odooConnection.getContext().setActiveTest( false );

    try {
      importReturnsIDS = odooConnection.getServerVersion().getMajor() >= 7;
    } catch ( XmlRpcException e ) {
      // Play it safe
      importReturnsIDS = false;
    }
  }

  public String[] getModelList() {

    String[] modelNames = new String[0];
    try {
      ObjectAdapter modelAdapter = new ObjectAdapter( odooConnection, "ir.model" );
      RowCollection rows = modelAdapter.searchAndReadObject( null, new String[]{ "model" } );
      modelNames = new String[rows.size()];
      for ( int i = 0; i < modelNames.length; i++ ) {
        modelNames[i] = rows.get( i ).get( "model" ).toString();
      }
      //CHECKSTYLE:EmptyBlock:OFF
    } catch ( Exception e ) {
    }

    return modelNames;
  }

  public int getModelDataCount( String model, FilterCollection filter ) throws XmlRpcException, OdooApiException {
    ObjectAdapter modelAdapter = new ObjectAdapter( odooConnection, model );
    return modelAdapter.getObjectCount( filter );
  }

  public RowCollection getModelData( String model, FilterCollection filter, ArrayList<FieldMapping> mappings,
                                     int offset, int limit ) throws XmlRpcException, OdooApiException {
    ArrayList<String> fieldList = new ArrayList<String>();
    for ( FieldMapping map : mappings ) {
      if ( !fieldList.contains( map.source_field ) ) {
        fieldList.add( map.source_field );
      }
    }

    String[] fieldStringList = new String[fieldList.size()];
    fieldStringList = fieldList.toArray( fieldStringList );

    ObjectAdapter modelAdapter = new ObjectAdapter( odooConnection, model );
    return modelAdapter.searchAndReadObject( filter, fieldStringList, offset, limit, null );
  }

  public RowCollection getModelData( String model, FilterCollection filter, String[] fieldStringList )
    throws XmlRpcException, OdooApiException {
    ObjectAdapter modelAdapter = new ObjectAdapter( odooConnection, model );
    return modelAdapter.searchAndReadObject( filter, fieldStringList );
  }

  public String[] getOutputFields( String model ) throws MalformedURLException, XmlRpcException, OdooApiException {
    ObjectAdapter modelAdapter = new ObjectAdapter( odooConnection, model );
    FieldCollection fields = modelAdapter.getFields();

    ArrayList<String> fieldArray = new ArrayList<String>();
    for ( Field field : fields ) {
      boolean readonly = field.getReadonly();

      // See if any of the states allows the readonly property to be false
      // If so, then we need to display the field as rows could potentially be in a
      // state that allows readonly to be false
      if ( readonly == true ) {
        for ( Object[] stateProperty : field.getStateProperties( "readonly" ) ) {
          boolean stateReadonly =
            ( (Boolean) ( stateProperty[1] instanceof Integer ? (Integer) stateProperty[1] == 1 : stateProperty[1] ) );
          readonly = readonly && stateReadonly;

          if ( readonly == false ) {
            break;
          }
        }
      }

      if ( field.getType() == FieldType.ONE2MANY || readonly == true ) {
        continue;
      }

      fieldArray.add( field.getName() );
    }

    // Sort the fields alphabetically
    Collections.sort( fieldArray );

    return fieldArray.toArray( new String[fieldArray.size()] );
  }

  public ObjectAdapter getAdapter( String objectName ) throws XmlRpcException, OdooApiException {
    return odooConnection.getObjectAdapter( objectName );
  }

  public void deleteObjects( String model, ArrayList<Object> ids ) throws XmlRpcException {
    commands.unlinkObject( model, ids.toArray( new Object[ids.size()] ) );
  }

  public boolean getImportReturnIDS() {
    return importReturnsIDS;
  }

  public ArrayList<FieldMapping> getDefaultFieldMappings( String model ) throws Exception {

    ArrayList<FieldMapping> mappings = new ArrayList<FieldMapping>();
    ObjectAdapter adapter = new ObjectAdapter( odooConnection, model );
    FieldCollection fields = adapter.getFields();

    FieldMapping fieldMap = new FieldMapping();
    fieldMap.source_model = model;
    fieldMap.source_field = "id";
    fieldMap.source_index = -1;
    fieldMap.target_model = model;
    fieldMap.target_field = "id";
    fieldMap.target_field_label = "Database ID";
    fieldMap.target_field_type = ValueMetaInterface.TYPE_INTEGER;
    mappings.add( fieldMap );

    for ( Field field : fields ) {
      fieldMap = new FieldMapping();
      String fieldName = field.getName();

      fieldMap.source_model = model;
      fieldMap.source_field = fieldName;
      fieldMap.source_index = -1;
      fieldMap.target_model = model;
      fieldMap.target_field = fieldName;
      fieldMap.target_field_label = field.getDescription();

      Field.FieldType fieldType = field.getType();

      switch ( fieldType ) {
        case CHAR:
        case TEXT:
        case BINARY: // Binaries are base64 encoded strings
          fieldMap.target_field_type = ValueMetaInterface.TYPE_STRING;
          mappings.add( fieldMap );
          break;
        case BOOLEAN:
          fieldMap.target_field_type = ValueMetaInterface.TYPE_BOOLEAN;
          mappings.add( fieldMap );
          break;
        case FLOAT:
          fieldMap.target_field_type = ValueMetaInterface.TYPE_NUMBER;
          mappings.add( fieldMap );
          break;
        case DATETIME:
        case DATE:
          fieldMap.target_field_type = ValueMetaInterface.TYPE_DATE;
          mappings.add( fieldMap );
          break;
        case MANY2ONE:
          FieldMapping newFieldMap = fieldMap.Clone();

          // Normal id field
          newFieldMap.source_index = 0;
          newFieldMap.target_model = field.getRelation();
          newFieldMap.target_field = fieldName + "_id";
          newFieldMap.target_field_label = field.getDescription() + "/Id";
          newFieldMap.target_field_type = ValueMetaInterface.TYPE_INTEGER;

          mappings.add( newFieldMap );

          // Add name field
          newFieldMap = fieldMap.Clone();

          newFieldMap.source_index = 1;
          newFieldMap.target_model = field.getRelation();
          newFieldMap.target_field = fieldName + "_name";
          newFieldMap.target_field_label = field.getDescription() + "/Name";
          newFieldMap.target_field_type = ValueMetaInterface.TYPE_STRING;

          mappings.add( newFieldMap );
          break;
        case ONE2MANY:
        case MANY2MANY:
        default:
          fieldMap.target_field_type = ValueMetaInterface.TYPE_STRING;
          mappings.add( fieldMap );
          break;
      }
    }

    return mappings;

  }
}
