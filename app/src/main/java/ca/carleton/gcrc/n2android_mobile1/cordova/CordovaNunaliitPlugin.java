package ca.carleton.gcrc.n2android_mobile1.cordova;

import android.app.Activity;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.List;
import java.util.Vector;

import ca.carleton.gcrc.n2android_mobile1.connection.Connection;
import ca.carleton.gcrc.n2android_mobile1.connection.DocumentDb;
import ca.carleton.gcrc.n2android_mobile1.couchbase.CouchbaseDocInfo;
import ca.carleton.gcrc.n2android_mobile1.couchbase.CouchbaseLiteService;
import ca.carleton.gcrc.n2android_mobile1.connection.ConnectionInfo;
import ca.carleton.gcrc.n2android_mobile1.activities.EmbeddedCordovaActivity;
import ca.carleton.gcrc.n2android_mobile1.couchbase.CouchbaseManager;
import ca.carleton.gcrc.n2android_mobile1.couchbase.CouchbaseQuery;
import ca.carleton.gcrc.n2android_mobile1.couchbase.CouchbaseQueryResults;

/**
 * Created by jpfiset on 3/14/16.
 */
public class CordovaNunaliitPlugin extends CordovaPlugin {

    final protected String TAG = this.getClass().getSimpleName();

    private CordovaInterface cordovaInterface = null;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        this.cordovaInterface = cordova;
        Log.i(TAG,"Cordova Interface: "+cordova.getClass().getSimpleName());

        Activity activity = cordova.getActivity();
        if( null != activity ){
            Log.i(TAG,"Activity: "+activity.getClass().getSimpleName());
        }
    }

    @Override
    public void pluginInitialize() {
        Log.i(TAG, "Plugin initialized. Service name: " + getServiceName());
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Log.v(TAG, "Action: " + action);

        if( "echo".equals(action) ) {
            String message = args.getString(0);
            this.echo(message, callbackContext);
            return true;

        } else if( "getConnectionInfo".equals(action) ) {
            this.getConnectionInfo(callbackContext);
            return true;

        } else if( "couchbaseGetDatabaseInfo".equals(action) ) {
            this.couchbaseGetDatabaseInfo(callbackContext);
            return true;

        } else if( "couchbaseGetDocumentRevision".equals(action) ) {
            String docId = args.getString(0);
            this.couchbaseGetDocumentRevision(docId, callbackContext);
            return true;

        } else if( "couchbaseCreateDocument".equals(action) ) {
            JSONObject doc = args.getJSONObject(0);
            this.couchbaseCreateDocument(doc, callbackContext);
            return true;

        } else if( "couchbaseUpdateDocument".equals(action) ) {
            JSONObject doc = args.getJSONObject(0);
            this.couchbaseUpdateDocument(doc, callbackContext);
            return true;

        } else if( "couchbaseDeleteDocument".equals(action) ) {
            JSONObject doc = args.getJSONObject(0);
            this.couchbaseDeleteDocument(doc, callbackContext);
            return true;

        } else if( "couchbaseGetDocument".equals(action) ) {
            String docId = args.getString(0);
            this.couchbaseGetDocument(docId, callbackContext);
            return true;

        } else if( "couchbaseGetDocuments".equals(action) ) {
            List<String> docIds = new Vector<String>();
            JSONArray jsonDocIds = args.getJSONArray(0);
            for(int i=0,e=jsonDocIds.length(); i<e; ++i){
                Object objId = jsonDocIds.get(i);
                if( objId instanceof String ){
                    String docId = (String)objId;
                    docIds.add(docId);
                } else {
                    String className = ""+objId;
                    if( null != objId ){
                        className = objId.getClass().getName();
                    }
                    Log.w(TAG, "couchbaseGetDocuments: invalid docId: "+className);
                }
            }
            this.couchbaseGetDocuments(docIds, callbackContext);
            return true;

        } else if( "couchbaseGetAllDocuments".equals(action) ) {
            this.couchbaseGetAllDocuments(callbackContext);
            return true;

        } else if( "couchbaseGetAllDocumentIds".equals(action) ) {
            this.couchbaseGetAllDocumentIds(callbackContext);
            return true;

        } else if( "couchbasePerformQuery".equals(action) ) {
            String designName = args.getString(0);
            JSONObject jsonQuery = args.getJSONObject(1);
            this.couchbasePerformQuery(designName, jsonQuery, callbackContext);
            return true;
        }

        return false;  // Returning false results in a "MethodNotFound" error.
    }

    private void echo(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    private void getConnectionInfo(CallbackContext callbackContext) {
        ConnectionInfo connInfo = retrieveConnection();

        if( null != connInfo ) {
            try {
                JSONObject result = new JSONObject();
                result.put("name", connInfo.getName());
                result.put("id", connInfo.getId());
                result.put("url", connInfo.getUrl());
                result.put("user", connInfo.getUser());
                callbackContext.success(result);
            } catch(Exception e) {
                callbackContext.error("Error while retrieving connection information: "+e.getMessage());
            }
        } else {
            callbackContext.error("Unable to retrieve connection information");
        }
    }

    private void couchbaseGetDatabaseInfo(CallbackContext callbackContext) {
        try {
            DocumentDb docDb = getDocumentDb();

            JSONObject dbInfo = docDb.getInfo();

            callbackContext.success(dbInfo);

        } catch(Exception e) {
            callbackContext.error("Error while performing couchbaseGetDatabaseInfo(): "+e.getMessage());
        }
    }

    private void couchbaseGetDocumentRevision(String docId, CallbackContext callbackContext) {
        try {
            DocumentDb docDb = getDocumentDb();

            String rev = docDb.getDocumentRevision(docId);

            JSONObject result = new JSONObject();
            result.put("rev", rev);
            callbackContext.success(result);
        } catch(Exception e) {
            callbackContext.error("Error while performing couchbaseGetDocumentRevision(): "+e.getMessage());
        }
    }

    private void couchbaseCreateDocument(JSONObject doc, CallbackContext callbackContext) {
        try {
            DocumentDb docDb = getDocumentDb();

            CouchbaseDocInfo info = docDb.createDocument(doc);

            JSONObject result = new JSONObject();
            result.put("id", info.getId());
            result.put("rev", info.getRev());
            callbackContext.success(result);

        } catch(Exception e) {
            callbackContext.error("Error while performing couchbaseCreateDocument(): "+e.getMessage());
        }
    }

    private void couchbaseUpdateDocument(JSONObject doc, CallbackContext callbackContext) {
        try {
            DocumentDb docDb = getDocumentDb();

            CouchbaseDocInfo info = docDb.updateDocument(doc);

            JSONObject result = new JSONObject();
            result.put("id", info.getId());
            result.put("rev", info.getRev());
            callbackContext.success(result);

        } catch(Exception e) {
            callbackContext.error("Error while performing couchbaseCreateDocument(): "+e.getMessage());
        }
    }

    private void couchbaseDeleteDocument(JSONObject doc, CallbackContext callbackContext) {
        try {
            DocumentDb docDb = getDocumentDb();

            CouchbaseDocInfo info = docDb.deleteDocument(doc);

            JSONObject result = new JSONObject();
            result.put("id", info.getId());
            result.put("rev", info.getRev());
            callbackContext.success(result);

        } catch(Exception e) {
            callbackContext.error("Error while performing couchbaseDeleteDocument(): "+e.getMessage());
        }
    }

    private void couchbaseGetDocument(String docId, CallbackContext callbackContext) {
        try {
            DocumentDb docDb = getDocumentDb();

            JSONObject doc = docDb.getDocument(docId);

            JSONObject result = new JSONObject();
            result.put("doc", doc);
            callbackContext.success(result);

        } catch(Exception e) {
            callbackContext.error("Error while performing couchbaseGetDocument(): "+e.getMessage());
        }
    }

    private void couchbaseGetDocuments(List<String> docIds, CallbackContext callbackContext) {
        try {
            DocumentDb docDb = getDocumentDb();

            List<JSONObject> docs = docDb.getDocuments(docIds);

            JSONArray jsonDocs = new JSONArray();
            for(JSONObject doc : docs){
                jsonDocs.put(doc);
            }


            JSONObject result = new JSONObject();
            result.put("docs", jsonDocs);
            callbackContext.success(result);

        } catch(Exception e) {
            callbackContext.error("Error while performing couchbaseGetDocuments(): "+e.getMessage());
        }
    }

    private void couchbaseGetAllDocuments(CallbackContext callbackContext) {
        try {
            DocumentDb docDb = getDocumentDb();

            List<JSONObject> docs = docDb.getAllDocuments();

            JSONArray jsonDocs = new JSONArray();
            for(JSONObject doc : docs){
                jsonDocs.put(doc);
            }


            JSONObject result = new JSONObject();
            result.put("docs", jsonDocs);
            callbackContext.success(result);

        } catch(Exception e) {
            callbackContext.error("Error while performing couchbaseGetAllDocuments(): "+e.getMessage());
        }
    }

    private void couchbaseGetAllDocumentIds(CallbackContext callbackContext) {
        try {
            DocumentDb docDb = getDocumentDb();

            List<String> docIds = docDb.getAllDocumentIds();

            JSONArray jsonIds = new JSONArray();
            for(String docId : docIds){
                jsonIds.put(docId);
            }

            JSONObject result = new JSONObject();
            result.put("ids", jsonIds);
            callbackContext.success(result);

        } catch(Exception e) {
            callbackContext.error("Error while performing couchbaseGetAllDocumentIds(): "+e.getMessage());
        }
    }

    private void couchbasePerformQuery(String designName, JSONObject jsonQuery, CallbackContext callbackContext) {
        try {
            CouchbaseQuery query = new CouchbaseQuery();

            // View name
            {
                String viewName = jsonQuery.getString("viewName");
                query.setViewName(viewName);
            }

            // Start key
            {
                Object startObj = jsonQuery.opt("startkey");
                if( null != startObj ){
                    query.setStartKey(startObj);
                }
            }

            // End key
            {
                Object endObj = jsonQuery.opt("endkey");
                if( null != endObj ){
                    query.setEndKey(endObj);
                }
            }

            // Keys
            {
                JSONArray keys = jsonQuery.optJSONArray("keys");
                if( null != keys ){
                    query.setKeys(keys);
                }
            }

            // Include Docs
            {
                boolean include_docs = jsonQuery.optBoolean("include_docs", false);
                if( include_docs ){
                    query.setIncludeDocs(true);
                }
            }

            // Limit
            {
                if( jsonQuery.has("limit") ) {
                    int limit = jsonQuery.getInt("limit");
                    query.setLimit(limit);
                }
            }

            // Reduce
            {
                boolean reduce = jsonQuery.optBoolean("reduce", false);
                if( reduce ){
                    query.setReduce(true);
                }
            }

            DocumentDb docDb = getDocumentDb();

            CouchbaseQueryResults results = docDb.performQuery(query);

            JSONArray jsonRows = new JSONArray();
            for(JSONObject row : results.getRows()){
                jsonRows.put(row);
            }

            JSONObject result = new JSONObject();
            result.put("rows", jsonRows);
            callbackContext.success(result);

        } catch(Exception e) {
            callbackContext.error("Error while performing couchbasePerformQuery(): "+e.getMessage());
        }
    }

    private DocumentDb getDocumentDb() throws Exception {
        ConnectionInfo connInfo = retrieveConnection();
        if( null == connInfo ){
            throw new Exception("Unable to retrieve connection information");
        }
        CouchbaseLiteService couchbaseService = getCouchDbService();
        if( null == couchbaseService ){
            throw new Exception("Unable to retrieve Couchbase service");
        }
        CouchbaseManager couchbaseManager = couchbaseService.getCouchbaseManager();
        Connection connection = new Connection(couchbaseManager, connInfo);
        DocumentDb docDb = connection.getLocalDocumentDb();
        return docDb;
    }

    public ConnectionInfo retrieveConnection(){
        ConnectionInfo connInfo = null;

        Activity activity = null;
        if( null != cordovaInterface ){
            activity = cordovaInterface.getActivity();
        }

        EmbeddedCordovaActivity cordovaActivity = null;
        if( null != activity && activity instanceof EmbeddedCordovaActivity ){
            cordovaActivity = (EmbeddedCordovaActivity)activity;
        }

        if( null != cordovaActivity ){
            connInfo = cordovaActivity.retrieveConnection();
        }

        return connInfo;
    }

    private CouchbaseLiteService getCouchDbService(){
        CouchbaseLiteService service = null;

        Activity activity = null;
        if( null != cordovaInterface ){
            activity = cordovaInterface.getActivity();
        }

        EmbeddedCordovaActivity cordovaActivity = null;
        if( null != activity && activity instanceof EmbeddedCordovaActivity ){
            cordovaActivity = (EmbeddedCordovaActivity)activity;
        }

        if( null != cordovaActivity ){
            service = cordovaActivity.getCouchDbService();
        }

        return service;
    }
}