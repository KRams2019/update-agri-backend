package com.mindtree.encryption.controller;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.http.ResponseEntity.HeadersBuilder;
import org.springframework.http.StreamingHttpOutputMessage.Body;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import com.mindtree.encryption.entity.FileInfo;
import com.mindtree.encryption.exception.EncryptionUtilException;
import com.mindtree.encryption.util.DateFormatter;
import com.mindtree.encryption.util.EncryptionDecryption;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import javax.activation.FileTypeMap;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.core.Response;


@CrossOrigin
@RestController
public class ImageController
{
	 private static final String UPLOADED_FOLDER = "../encryption/src/main/java/com/mindtree/encryption/images/";
	 private static String token=EncryptionDecryption.getAlphaNumericString();
	 private static String dateTime="";
	 private final static Path root = Paths.get(UPLOADED_FOLDER);
	 
	 @Value("${app.id}")
	 private String appId;
		
	@Value("${app.password}")
	private String appPassword;
	 
	
	 @GetMapping("/delete-all-pics/{passedToken}")
	 public ResponseEntity<Map<String,Object>> deletePics(@PathVariable String passedToken)
	 {
		 
		Map<String, Object> responseMap =  new HashMap<String, Object>();
		 if(checkToken_(passedToken)==false)
		{
			 responseMap.put("message","failed to delete pics");
			 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
		}
		 responseMap.put("message",deleteAllPics());
		 return   ResponseEntity.status(HttpStatus.OK).body(responseMap);
	 }
	
	 @GetMapping("/logout/{passedToken}")
	 public ResponseEntity<Map<String,Object>> logout(@PathVariable String passedToken)
	 {
		 System.err.println("logout..............");
		Map<String,Object> responseMap = new HashMap<String,Object>();
		responseMap.put("date",DateFormatter.convertUtilDateToString(new Date()));
		responseMap.put("message","successfull logout");
		if(checkToken_(passedToken)==false)
		{
			responseMap.put("message","fail to logout");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
		}
		 token = EncryptionDecryption.getAlphaNumericString();

		 return   ResponseEntity.status(HttpStatus.OK).body(responseMap);

	 }
	
	
	@GetMapping("/delete-image/{imgName}/{passedToken}")
	public ResponseEntity<Map<String,Object>> deleteImg(@PathVariable String imgName,
			@PathVariable String passedToken) 
	{
		Map<String,Object> responseMap = new HashMap<String,Object>();
		responseMap.put("message","image deleted successfully");
		if(checkToken_(passedToken.trim())==false)
		{
			responseMap.put("message","failed to  delete image");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
		}
		imgName = imgName.trim();
		try {
            Files.delete(Paths.get(UPLOADED_FOLDER+imgName));
        } catch (IOException e) {
            e.printStackTrace();
        }
		return ResponseEntity.status(HttpStatus.OK).body(responseMap);
	}
	
	/*
	 * @CrossOrigin(origins = "*", allowedHeaders = "*")
	 * 
	 * @GetMapping("consume-image") public ResponseEntity<byte[]> getImage() throws
	 * IOException{ File img =new File(UPLOADED_FOLDER+getAllFileNames().get(0));
	 * return ResponseEntity. ok().
	 * contentType(MediaType.valueOf(FileTypeMap.getDefaultFileTypeMap().
	 * getContentType(img))) .body(Files.readAllBytes(img.toPath())); }
	 */
	
	/*
	 * @CrossOrigin(origins = "*", allowedHeaders = "*",methods = RequestMethod.GET)
	 * 
	 * @GetMapping("consume-images") public ResponseEntity<byte[]> getImages()
	 * throws IOException{ File img = null; List<Byte[]> byteList = new
	 * ArrayList<Byte[]>(); for(String fileName:getAllFileNames()) { img = new
	 * File(UPLOADED_FOLDER+fileName); // Byte byte_[] = new Byte[]; //
	 * byteList.add(Files.readAllBytes(img.toPath())); } return ResponseEntity.
	 * ok(). contentType(MediaType.valueOf(FileTypeMap.getDefaultFileTypeMap().
	 * getContentType(img))) .body(Files.readAllBytes(img.toPath())); }
	 */
	

	@GetMapping("get-file-list")
	public ResponseEntity<List<String>> getFileNames() throws IOException
	{
		return ResponseEntity.ok().body(getAllFileNames());
	}
	


	@PostMapping(value = "uploadMultipleFiles")
    public ResponseEntity<?> uploadFileMulti(@RequestParam(value = "files1",required = true) MultipartFile[] images,
    		@RequestParam(value = "passedToken") String passedToken) 
	{
		
		  if(checkToken_(passedToken)==false) 
		  { 
			  return new ResponseEntity("Session_Expired", HttpStatus.BAD_REQUEST);
			  }
		 
		String message = "";
		try 
		{
			message  = saveUploadedFiles(Arrays.asList(images));
			System.out.println(images.length);
		} 
		catch (IOException e)
		{
			message = "failed to upload";
			e.printStackTrace();
			System.err.println("save-upload-called");
		}
        return new ResponseEntity(message, HttpStatus.OK);

    }

	private static String saveUploadedFiles(List<MultipartFile> files) throws IOException
	{
		for (MultipartFile file : files) {

			 if (file==null)
			 {
				 System.out.println("no file selected");
		      }
			 try {
						byte[] bytes = file.getBytes();
				         Path path = Paths.get(UPLOADED_FOLDER + file.getOriginalFilename());
				         Files.write(path, bytes);
		
					}
					 
				 
		catch (IOException e)
		{
	         e.printStackTrace();
	     }	
	}
		return "files uploaded successfully";
	}
	
	private static List<String> getAllFileNames()
	{
		File directoryPath = new File(UPLOADED_FOLDER);
		ArrayList<String> fileList = new ArrayList<String>();
		for (String string : directoryPath.list())
		{
			fileList.add(string);
		}
		return fileList;
	}
	
	private String deleteAllPics() 
	{
		String message = "";
		File directoryPath = new File(UPLOADED_FOLDER);
		List<String> fileList =  getAllFileNames();
		for (String string : directoryPath.list())
		{
		  try {
			Files.delete(Paths.get(UPLOADED_FOLDER+string));
		}
		  catch (IOException e)
		  {
		
			e.printStackTrace();
		}
		}
		return "All files deleted";
	}
	


	@PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> map)
	{

		Map<String, String> responseMap = new HashMap<String, String>();
		Map<String, String> errorResponseMap = new HashMap<String, String>();
		String id = (String)map.get("id");
		String password = (String)map.get("password");
		id=id.trim();
		password=password.trim();
		if(id.equals(appId) && password.equals(appPassword))
		{
			String key=EncryptionDecryption.getAlphaNumericString();
			try 
			{
				ImageController.token=EncryptionDecryption.encryptData(key);
				dateTime = DateFormatter.convertUtilDateToString(new Date());
				responseMap.put("token", token);
			} catch (EncryptionUtilException e) 
			{
				e.printStackTrace();
			}
			return ResponseEntity.status(HttpStatus.OK).body(responseMap);
		}
		else 
		{
			errorResponseMap.put("message","invalid credential");
			errorResponseMap.put("date_time",DateFormatter.convertUtilDateToString(new Date()));
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponseMap); 
		}
	}

	@PostMapping(value="/token-check",consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Boolean> checkToken(@RequestBody Map<String, String> map)
	{
	
		String passedToken = map.get("passedToken");
		if(passedToken.compareTo(ImageController.token)==0&&DateFormatter.checkTimeDiff(dateTime,DateFormatter.convertUtilDateToString(new Date()))) 
		{
			return ResponseEntity.status(HttpStatus.OK).body(true);
		}
		else 
		{
			System.err.println("bad request");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
		}
	}
	private static boolean checkToken_(String passedToken)
	{
	
		passedToken = passedToken.trim();
		
		if(passedToken.compareTo(ImageController.token)==0
				&&
				DateFormatter.checkTimeDiff(dateTime,DateFormatter.convertUtilDateToString(new Date()))) 
		{
			return true;
		}
		else 
		{
			return false;
		}
	}

	@GetMapping("/files")
	public ResponseEntity<List<FileInfo>> getListFiles() 
	{
	  List<FileInfo> fileInfos = loadAll().map(path -> {
	    String filename = path.getFileName().toString();
	    String url = MvcUriComponentsBuilder
	        .fromMethodName(ImageController.class, "getFile", path.getFileName().toString()).build().toString();
	    return new FileInfo(filename, url);
	  }).collect(Collectors.toList());
	  return ResponseEntity.status(HttpStatus.OK).body(fileInfos);
	}
	
	
	@GetMapping("/files/{filename:.+}")
	public ResponseEntity<Resource> getFile(@PathVariable String filename) {
	  Resource file = load(filename);
	  return ResponseEntity.ok()
	      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"").body(file);
	}
	public static Resource load(String filename) {
		  try {
		    Path file = root.resolve(filename);
		    Resource resource = new UrlResource(file.toUri());
		    if (resource.exists() || resource.isReadable()) {
		      return resource;
		    } else {
		      throw new RuntimeException("Could not read the file!");
		    }
		  } catch (MalformedURLException e) {
		    throw new RuntimeException("Error: " + e.getMessage());
		  }
		}
	public static Stream<Path> loadAll() {
	
		  try {
		    return Files.walk(root, 1).filter(path -> !path.equals(root)).map(root::relativize);
		  } catch (IOException e) {
		    throw new RuntimeException("Could not load the files!");
		  }
	
		}
}
