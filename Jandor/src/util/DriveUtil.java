package util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import session.User;
import canvas.CardLayer;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.AddSheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetResponse;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesResponse;
import com.google.api.services.sheets.v4.model.DeleteSheetRequest;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.ValueRange;

import drive.DefaultSheet;
import drive.FileListener;

public class DriveUtil {

	static {
		// Fix buggy logger for warning on windows
		final java.util.logging.Logger buggyLogger = java.util.logging.Logger.getLogger(FileDataStoreFactory.class.getName());
		buggyLogger.setLevel(java.util.logging.Level.SEVERE);
	}

	public static final long CHANGE_LISTENER_TIMEOUT = 10000;
	public static final long INBOX_LISTENER_TIMEOUT = 1000;

	public static final String TAB_DEFAULT = "Sheet1";

	//public static final String DOWNLOAD_NAME_ALL_CARDS_X_JSON = FileUtil.RESOURCE_CARDS_JSONS;
	//public static final String DOWNLOAD_NAME_ALL_SETS_JSON = FileUtil.RESOURCE_SETS_JSONS;

	public static final String FOLDER_ID_INBOXES = "0B7KQ1AZJL7icVlczR1lVNWI1LU0";
	public static final String FOLDER_ID_BACKUPS = "0B7KQ1AZJL7icOXZFNFhNYUpTYkU";

	public static final String SHEET_ID_USERS = "1q9d7gM0z2t7oo_T8DFPDpWjW1C7mqqvrh_PDWtzbOtg";
	public static final String TAB_NAME_USERS = "Users";
	public static final String START_RANGE_USERS = "A";
	public static final String END_RANGE_USERS = "F";

	public static final String TAB_NAME_INBOX = "Inbox";
	public static final String START_RANGE_INBOX = "A";
	public static final String END_RANGE_INBOX = "B";

	private static final Map<String, List<FileListener>> fileListenersByFileId = new HashMap<String, List<FileListener>>();
	private static final List<FileListener> emptyListeners = new ArrayList<FileListener>();

	public static void init() {
		startListeningForInboxChanges();
	}

	public static synchronized void addFileListener(FileListener listener) {
		if(!fileListenersByFileId.containsKey(listener.getFileId())) {
			fileListenersByFileId.put(listener.getFileId(), new ArrayList<FileListener>());
		}
		fileListenersByFileId.get(listener.getFileId()).add(listener);
	}

	public static synchronized void removeFileListener(FileListener listener) {
		if(fileListenersByFileId.containsKey(listener.getFileId()) &&
			fileListenersByFileId.get(listener.getFileId()).contains(listener)) {
			fileListenersByFileId.get(listener.getFileId()).remove(listener);
		}
	}

	public static synchronized List<FileListener> getFileListeners(String fileId) {
		if(!fileListenersByFileId.containsKey(fileId)) {
			return emptyListeners;
		}
		return fileListenersByFileId.get(fileId);
	}

	public static final String toInboxFolderId(User user) {
		return DriveUtil.toFileId(toInboxFolderName(user));
	}

	public static final String toInboxFolderName(User user) {
		return "Inbox-" + user.getGUID();
	}

	public static final String toBackupFolderId(User user) {
		return DriveUtil.toFileId(toBackupFolderName(user));
	}

	public static final String toBackupFolderName(User user) {
		return "Backup-" + user.getGUID();
	}

	private DriveUtil() {}

    /** Application name. */
    private static final String APPLICATION_NAME = "Jandor";

    /** Directory to store user credentials for this application. */
    /*private static final java.io.File DATA_STORE_DIR = new java.io.File(
        System.getProperty("user.home"), ".credentials/sheets.googleapis.com-java-quickstart");*/
    private static final java.io.File DATA_STORE_DIR = FileUtil.getCredentialsFolder();

    private static Credential credential = null;

    /** Global instance of the {@link FileDataStoreFactory}. */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY =
        JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;

    /** Global instance of the scopes required by this quickstart.
     *
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/sheets.googleapis.com-java-quickstart
     */
    private static final List<String> SCOPES =
        Arrays.asList(SheetsScopes.SPREADSHEETS, DriveScopes.DRIVE, DriveScopes.DRIVE_METADATA);

    private static Sheets sheetService = null;
    private static Drive driveService = null;

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    private static Credential authorize() throws IOException {
    	if(credential == null) {

	        // Load client secrets.
	        InputStream in =
	            DriveUtil.class.getResourceAsStream("/client_secret.json");
	        GoogleClientSecrets clientSecrets =
	            GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

	        // Build flow and trigger user authorization request.
	        GoogleAuthorizationCodeFlow flow =
	                new GoogleAuthorizationCodeFlow.Builder(
	                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
	                .setDataStoreFactory(DATA_STORE_FACTORY)
	                .setAccessType("offline")
	                .build();
	        credential = new AuthorizationCodeInstalledApp(
	            flow, new LocalServerReceiver()).authorize("user");
	        System.out.println(
	                "Found Google API Credentials in " + DATA_STORE_DIR.getAbsolutePath());
    	}
        return credential;
    }

    /**
     * Build and return an authorized Sheets API client service.
     * @return an authorized Sheets API client service
     * @throws IOException
     */
    public static Sheets getSheetsService() throws IOException {
    	if(sheetService == null) {
    		Credential credential = authorize();
    		sheetService = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    	}
        return sheetService;
    }

    /**
     * Build and return an authorized Drive client service.
     * @return an authorized Drive client service
     * @throws IOException
     */
    public static Drive getDriveService() throws IOException {
    	if(driveService == null) {
    		Credential credential = authorize();
    		driveService = new Drive.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    	}
    	return driveService;
    }

    public static List<List<Object>> getValues(String spreadsheetId, String tabName, String rangeStart, String rangeEnd) {
    	List<List<Object>> values = null;
    	try {
    		Sheets service = getSheetsService();
    		String range = tabName + "!" + rangeStart + ":" + rangeEnd;
    		ValueRange response = service.spreadsheets().values()
    								.get(spreadsheetId, range)
    								.execute();
    		values = response.getValues();
    	} catch (IOException e) {
			if(!handleNotFound(e, spreadsheetId, null)) {
				e.printStackTrace();
			}
		}
    	return values;
    }

    public static void setValue(String spreadsheetId, String tabName, String rangeStart, String rangeEnd, DefaultSheet sheet) {
    	try {
    		Sheets service = getSheetsService();
    		String range = tabName + "!" + rangeStart + ":" + rangeEnd;

    		List<List<Object>> arrData = sheet.getRawData();

      	  	ValueRange oRange = new ValueRange();
      	  	oRange.setRange(range); // I NEED THE NUMBER OF THE LAST ROW
      	  	oRange.setValues(arrData);

      	  	List<ValueRange> oList = new ArrayList<>();
      	  	oList.add(oRange);

      	  	BatchUpdateValuesRequest oRequest = new BatchUpdateValuesRequest();
      	  	oRequest.setValueInputOption("RAW");
      	  	oRequest.setData(oList);

      	  	BatchUpdateValuesResponse oResp1 = service.spreadsheets().values().batchUpdate(spreadsheetId, oRequest)
    												.execute();
    	} catch (IOException e) {
			if(!handleNotFound(e, spreadsheetId, null)) {
				e.printStackTrace();
			}
		}
    }

    public static void setValue(String spreadsheetId, String tabName, String... values) {
    	if(values.length == 0) {
    		return;
    	}

    	String rangeStart = "A";
    	String rangeEnd = String.valueOf((char)('A' + (values.length - 1)));

    	try {
    		Sheets service = getSheetsService();
    		String range = tabName + "!" + rangeStart + ":" + rangeEnd;

    		List<List<Object>> arrData = new ArrayList<List<Object>>();
    		arrData.add(new ArrayList<Object>());
    		for(String value : values) {
    			arrData.get(0).add(value);
    		}

      	  	ValueRange oRange = new ValueRange();
      	  	oRange.setRange(range); // I NEED THE NUMBER OF THE LAST ROW
      	  	oRange.setValues(arrData);

      	  	List<ValueRange> oList = new ArrayList<>();
      	  	oList.add(oRange);

      	  	BatchUpdateValuesRequest oRequest = new BatchUpdateValuesRequest();
      	  	oRequest.setValueInputOption("RAW");
      	  	oRequest.setData(oList);

      	  	BatchUpdateValuesResponse oResp1 = service.spreadsheets().values().batchUpdate(spreadsheetId, oRequest)
    												.execute();
    	} catch (IOException e) {
			if(!handleNotFound(e, spreadsheetId, null)) {
				e.printStackTrace();
			}
		}
    }

    public static String createSheet(String spreadsheetName, String tabName, String... headers) {
    	String sheetId = createSheet(spreadsheetName, tabName);
    	setValue(sheetId, tabName, headers);
    	return sheetId;
    }

    public static String createSheet(String spreadsheetName, String tabName) {
    	String sheetId = DriveUtil.createSheet(spreadsheetName);
		if(sheetId == null) {
			System.err.println("Could not create spreadsheet \"" + spreadsheetName + "\".");
		} else {
			DriveUtil.createTab(sheetId, tabName);
			DriveUtil.deleteTab(sheetId, DriveUtil.TAB_DEFAULT);
		}
		return sheetId;
    }

    public static String createSheet(String spreadsheetName) {
    	String sheetId = null;
    	try {
    		Sheets service = getSheetsService();

    		Spreadsheet sheet = new Spreadsheet();
    		SpreadsheetProperties properties = new SpreadsheetProperties();
    		properties.setTitle(spreadsheetName);
    		sheet.setProperties(properties);

    		Spreadsheet response = service.spreadsheets().create(sheet).execute();
    		sheetId = response.getSpreadsheetId();
    	} catch (IOException e) {
			if(!handleNotFound(e, sheetId, spreadsheetName)) {
				e.printStackTrace();
			}
		}
    	return sheetId;
    }

    public static void createTab(String spreadsheetId, String tabName) {
    	AddSheetRequest sheetRequest = new AddSheetRequest();
		SheetProperties properties = new SheetProperties();
		properties.setTitle(tabName);
		sheetRequest.setProperties(properties);

		Request request = new Request();
		request.setAddSheet(sheetRequest);

		performRequest(spreadsheetId, request);
    }

    public static void deleteTab(String spreadsheetId, String tabName) {
    	DeleteSheetRequest sheetRequest = new DeleteSheetRequest().setSheetId(getTabId(spreadsheetId, tabName));

		Request request = new Request();
		request.setDeleteSheet(sheetRequest);

		performRequest(spreadsheetId, request);
    }

    public static Integer getTabId(String spreadsheetId, String tabName) {
    	return getTabIdsByName(spreadsheetId).get(tabName);
    }

    public static Map<String, Integer> getTabIdsByName(String spreadsheetId) {
    	Map<String, Integer> tabIdsByName = new LinkedHashMap<String, Integer>();
    	Sheets service;
		try {
			service = getSheetsService();
			Spreadsheet spreadsheet = service.spreadsheets().get(spreadsheetId).execute();
			List<Sheet> sheets = (List<Sheet>) spreadsheet.get("sheets");
			for(Sheet sheet : sheets) {
				tabIdsByName.put(sheet.getProperties().getTitle(), sheet.getProperties().getSheetId());
			}
		} catch (IOException e) {
			if(!handleNotFound(e, spreadsheetId, null)) {
				e.printStackTrace();
			}
		}

		return tabIdsByName;
    }

    private static void performRequest(String spreadsheetId, Request request) {
    	try {
    		Sheets service = getSheetsService();

    		List<Request> requests = new ArrayList<>();
    		requests.add(request);
    		BatchUpdateSpreadsheetRequest oRequest = new BatchUpdateSpreadsheetRequest();
      	  	oRequest.setRequests(requests);
      	  	BatchUpdateSpreadsheetResponse oResp1 = service.spreadsheets().batchUpdate(spreadsheetId, oRequest)
    												.execute();
    	} catch (IOException e) {
			if(!handleNotFound(e, spreadsheetId, null)) {
				e.printStackTrace();
			}
		}
    }

    public static void startListeningForInboxChanges() {
		if(DebugUtil.OFFLINE_MODE) {
			return;
		}

		new Thread() {

    		@Override
			public void run() {
    			System.out.println("Started listening to user's inbox");
    			try {
					while(true) {
						if(LoginUtil.isLoggedIn()) {
							List<File> files = getFilesInFolder(LoginUtil.getUser().getInboxFolderId());
							if(files != null && !files.isEmpty()) {
								MailUtil.receiveFilesFromDrive(files);
							}

							/*if(!getFileListeners(inboxId).isEmpty()) {
								List<File> files = getFilesInFolder(LoginUtil.getUser().getInboxId());
								if(files != null && !files.isEmpty()) {
									for(FileListener listener : getFileListeners(inboxId)) {
				    	        		listener.callback(files);
				    	        	}
								}
							}*/
						}
						Thread.sleep(INBOX_LISTENER_TIMEOUT);
					}
    			} catch (InterruptedException e) {
    				e.printStackTrace();
    			}
    		}

	    }.start();
	}

    public static List<File> getFiles() {
        Drive service = null;
        FileList result = null;

        try {
			// Build a new authorized API client service.
			service = getDriveService();

			// Print the names and IDs for up to 10 files.
			result = service.files().list()
				 .setFields("nextPageToken, files(id, name)")
			     .execute();
		} catch (IOException e) {
			e.printStackTrace();
		}

        if(service == null || result == null) {
        	return null;
        }

        List<File> files = result.getFiles();
        if (files == null || files.size() == 0) {
            //System.out.println("No files found.");
        } else {
            System.out.println("Files:");
            for (File file : files) {
                System.out.printf("%s (%s)\n", file.getName(), file.getId());
            }
        }

        return files;
    }

    public static List<File> getFilesInFolder(String folderId) {
        Drive service = null;
        FileList result = null;

        try {
			// Build a new authorized API client service.
			service = getDriveService();

			// Print the names and IDs for up to 10 files.
			result = service.files().list()
				 .setQ("'" + folderId + "' in parents")
				 .setFields("nextPageToken, files(id, name)")
			     .execute();
		} catch (IOException e) {
			if(!handleNotFound(e, folderId, null)) {
				e.printStackTrace();
			}
		}

        if(service == null || result == null) {
        	return null;
        }

        List<File> files = result.getFiles();
        if (files == null || files.size() == 0) {
            //System.out.println("No files found.");
        } else {
            System.out.println("Files:");
            for (File file : files) {
                System.out.printf("%s (%s)\n", file.getName(), file.getId());
            }
        }

        return files;
    }

    private static boolean handleNotFound(IOException e, String fileId, String fileName) {
    	boolean notFound = false;
    	String cause = null;
    	if(e instanceof HttpResponseException && ((HttpResponseException) e).getStatusCode() == 404) {
    		notFound = true;
    		cause = "File not found for";
    	} else if(e instanceof SocketTimeoutException && e.getCause() instanceof GoogleJsonResponseException) {
			GoogleJsonResponseException g = (GoogleJsonResponseException) e;
			if(g.getStatusCode() == 404) {
				notFound = true;
				cause = "File not found for";
			} else {
				cause = "Socket timed out for";
			}
    	} else if(e instanceof SocketTimeoutException) {
    		cause = "Socket timed out for";
    	}
    	if(!notFound) {
    		return false;
    	}

    	if(fileId == null && fileName == null) {
    		System.err.println(cause + " fileId : null, fileName: null");
    	} else if(fileId == null) {
    		System.err.println(cause + " fileName: " + fileName);
    	} else if(fileName == null) {
    		System.err.println(cause + " fileId: " + fileId);
    	} else {
    		System.err.println(cause + " fileId: " + fileId + " and fileName: " + fileName);
    	}
    	return true;
    }

    private static Map<String, String> getFileIdsByName() {
    	Map<String, String> sheetIdsByName = new HashMap<String, String>();
    	for(File file : getFiles()) {
    		sheetIdsByName.put(file.getName(), file.getId());
    	}
    	return sheetIdsByName;
    }

    public static String toFileId(String fileName) {
    	File file = findFile(fileName);
    	return file == null ? null : file.getId();
    }

    private static File findFile(String fileName) {
    	try {
    		if(fileName == null) {
    			return null;
    		}
			FileList result = getDriveService().files().list().setQ("name='" + fileName + "'").execute();
			if(result == null || result.getFiles() == null || result.getFiles().size() == 0) {
				return null;
			}
			return result.getFiles().get(0);
    	} catch (IOException e) {
			if(!handleNotFound(e, null, fileName)) {
				e.printStackTrace();
			}
		}
    	return null;
    }

    public static boolean exists(String fileName) {
    	return findFile(fileName) != null;
    }

    public static void moveFile(String fileId, String folderId) {
    	try {
	    	File file = getDriveService().files().get(fileId)
	    	        .setFields("parents")
	    	        .execute();

	    	StringBuilder previousParents = new StringBuilder();
	    	for(String parent: file.getParents()) {
	    	    previousParents.append(parent);
	    	    previousParents.append(',');
	    	}

	    	// Move the file to the new folder
	    	file = getDriveService().files().update(fileId, null)
	    	        .setAddParents(folderId)
	    	        .setRemoveParents(previousParents.toString())
	    	        .setFields("id, parents")
	    	        .execute();
    	} catch(IOException e) {
    		e.printStackTrace();
    	}
    }

    public static String createFile(String fileName, String content) {
    	return createFile(fileName, content, null);
    }

    public static String createFile(String fileName, String content, String folderId) {
    	try {
    		File body = new File();
    		body.setName(fileName);
    		body.setDescription(fileName);
    		body.setMimeType("text/plain");

    		final String textContent = content;
    		ByteArrayInputStream bis = new ByteArrayInputStream(textContent.getBytes("UTF-8"));
    		InputStreamContent isc = new InputStreamContent(null, bis);
    		File file = getDriveService().files().create(body, isc).execute();

    		if(folderId != null) {
    			moveFile(file.getId(), folderId);
    		}

    		return file.getId();
    	} catch(IOException e) {
    		e.printStackTrace();
    	}
    	return null;
    }

    public static void deleteFile(String fileId) {
    	try {
    		getDriveService().files().delete(fileId).execute();
    	} catch(IOException e) {
    		e.printStackTrace();
    	}
    }

    public static String createFolder(String folderName, String parentFolderId) {
    	File fileMetadata = new File();
    	fileMetadata.setName(folderName);
    	fileMetadata.setMimeType("application/vnd.google-apps.folder");

    	File file = null;
		try {
			file = getDriveService().files().create(fileMetadata)
					.setFields("id")
			        .execute();

			if(parentFolderId != null) {
				moveFile(file.getId(), parentFolderId);
			}

		} catch (IOException e) {
			if(!handleNotFound(e, file == null ? null : file.getId(), folderName)) {
				e.printStackTrace();
			}
		}

		if(file == null) {
			return null;
		}
    	return file.getId();
    }

    public static String getFileContent(String fileId) {
    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    	try {
    		getDriveService().files().get(fileId).executeMediaAndDownloadTo(outputStream);
    		return outputStream.toString();
    	} catch (IOException e) {
			if(!handleNotFound(e, fileId, null)) {
				e.printStackTrace();
			}
		}
    	return null;
    }

    public static void main2(String[] args) throws IOException {
        // Build a new authorized API client service.
        Sheets service = getSheetsService();

        // Prints the names and majors of students in a sample spreadsheet:
        // https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
        //String spreadsheetId = "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms";
        //String range = "Class Data!A2:E";

        // jandor.saddlebags: Database/Resources/Users
        String spreadsheetId = "1q9d7gM0z2t7oo_T8DFPDpWjW1C7mqqvrh_PDWtzbOtg";
        String range = "Users!A1:E";

        ValueRange response = service.spreadsheets().values()
            .get(spreadsheetId, range)
            .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.size() == 0) {
            System.out.println("No data found.");
        } else {
        	for (List row : values) {
            // Print columns A and E, which correspond to indices 0 and 4.
            System.out.printf("%s, %s\n", row.get(0), row.get(4));
          }
        }
    }

    public static void main(String[] args) throws IOException {
    	System.out.println("Files:");
    	for (File file : getFiles()) {
    		System.out.printf("%s (%s)\n", file.getName(), file.getId());
        }
    }

}