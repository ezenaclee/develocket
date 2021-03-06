package com.ezen.develocket.inquiry.controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.ezen.develocket.inquiry.service.InquiryService;
import com.ezen.develocket.inquiry.vo.InquiryImgVO;
import com.ezen.develocket.inquiry.vo.InquiryVO;
import com.ezen.develocket.rocketInfo.vo.RocketInfoVO;



@Controller("InquiryController")
public class InquiryControllerImpl implements InquiryController {
	
	@Autowired
	private InquiryService inquiryService;
	@Autowired
	private InquiryVO inquiryVO;

	private static String INQUIRY_IMAGE_REPO = "C:\\develocket_repo\\inquiry";
	
							 
	@RequestMapping(value = "/inquiry/*Form.do", method = {RequestMethod.GET, RequestMethod.POST})
	public ModelAndView form(@RequestParam(value = "parent_cd", required = false) String parent_cd,
							 @RequestParam(value = "group_cd", required = false) String group_cd,
							 HttpServletRequest request,
			 				 HttpServletResponse response) throws Exception {
		
		String viewName = (String) request.getAttribute("viewName");
		
		System.out.println("viewName: " + viewName);
		HttpSession session = request.getSession();
		
		if (viewName.equals("/inquiry/inquiryForm")) {
			
			if (session.getAttribute("parent_cd") != null) {
			session.removeAttribute("parent_cd"); }
			  
			if (session.getAttribute("group_cd") != null) {
			session.removeAttribute("group_cd"); }
			 		
		}
		
		if (viewName.equals("/inquiry/inquiryReplyForm")) {
			if(parent_cd != null) {
				session.setAttribute("parent_cd", parent_cd);	// ?????????????????? ????????? ??????(?????????)
			}
			
			if (group_cd != null) {
				session.setAttribute("group_cd", group_cd);
			}
		}
		
		ModelAndView mav = new ModelAndView(viewName);
		
		return mav;
	}


	@Override
	@RequestMapping(value = "/inquiry/inquiryList.do", method = {RequestMethod.GET, RequestMethod.POST})
	public ModelAndView inquiryList(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		// section?????? pageNum ?????? ??????
		String _section = request.getParameter("section");
		String _pageNum = request.getParameter("pageNum");
		
		// ?????? ?????? ??? section?????? pageNum?????? ????????? ?????? 1??? ????????????.
		int section = Integer.parseInt(((_section == null) ? "1" : _section ));
		int pageNum = Integer.parseInt(((_pageNum == null) ? "1" : _pageNum ));
		
		// section?????? pageNum ?????? HashMap??? ?????????.
		Map<String, Integer> pagingMap = new HashMap<>();
		pagingMap.put("section", section);
		pagingMap.put("pageNum", pageNum);
		
		Map<String, Object> inquiryMap = inquiryService.inquiryList(pagingMap);
		inquiryMap.put("section", section);
		inquiryMap.put("pageNum", pageNum);
		
		String viewName = (String) request.getAttribute("viewName");
		System.out.println("viewName2: " + viewName);
		
		ModelAndView mav = new ModelAndView(viewName);
		mav.addObject("inquiryMap", inquiryMap);
		
		return mav;
	}


	@Override
	@RequestMapping(value = "/inquiry/addNewInquiry.do", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity addNewInquiry(MultipartHttpServletRequest multipartRequest,
										HttpServletResponse response) throws Exception {


		multipartRequest.setCharacterEncoding("UTF-8");
		String imageFileName = null;
		
		Map<String, Object> inquiryMap = new HashMap<>();
		
		Enumeration enu =  multipartRequest.getParameterNames();
		while (enu.hasMoreElements()) {
			String name = (String) enu.nextElement();
			String value = multipartRequest.getParameter(name);
			
			inquiryMap.put(name, value);
		}
		
		// ????????? ??? ????????? ????????? ?????????????????? ?????????(?????????)??? ???????????? inquiryMap??? ?????????
		HttpSession session = multipartRequest.getSession();
		RocketInfoVO rocketInfoVO = (RocketInfoVO) session.getAttribute("rocketInfoVO");
		String rocket_cd = rocketInfoVO.getRocket_cd();
		inquiryMap.put("rocket_cd", rocket_cd);
		
		String parent_cd = (String) session.getAttribute("parent_cd");
		inquiryMap.put("parent_cd", (parent_cd == null ? "0" : parent_cd));
		
		// TODO self ??????!!
		String group_cd = (String) session.getAttribute("group_cd");
		inquiryMap.put("group_cd", (group_cd == null ? "0" : group_cd));
		
		List<String> fileList = upload(multipartRequest);
		List<InquiryImgVO> imageFileList = new ArrayList<>();
		
		if (fileList != null && fileList.size() != 0) {
			for (String fileName : fileList) {
				if (!fileName.equals("")) {
					InquiryImgVO inquiryImgVO = new InquiryImgVO();
					inquiryImgVO.setImageFileName(fileName);

					imageFileList.add(inquiryImgVO);
				}
			}
			
			inquiryMap.put("imageFileList", imageFileList);
		}
		
		// map??? ??????
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "text/html;charset=UTF-8");
		
		String message = "";
		ResponseEntity resEnt = null;
		
		try {
			// ????????? ??????
			String inquiry_cd = inquiryService.addNewInquiry(inquiryMap);
			
			if (imageFileList != null && imageFileList.size() != 0) {
				for (InquiryImgVO inquiryImgVO : imageFileList) {
					imageFileName = inquiryImgVO.getImageFileName();
					
					// temp(?????? ??????)??? ?????? ???????????? inquiryNO ????????? ????????????
					File srcFile = new File(INQUIRY_IMAGE_REPO + "\\temp\\" + imageFileName);
					File destFile = new File(INQUIRY_IMAGE_REPO + "\\" + inquiry_cd);
					FileUtils.moveFileToDirectory(srcFile, destFile, true);
					
					
				}
			}
			
			if (inquiryMap.get("parent_cd") == "0") {
				message = "<script>";
				message += "alert('????????? ??????????????????.');";
				message += "location.href = '"+ multipartRequest.getContextPath() + "/inquiry/inquiryList.do';";
				message += "</script>";
			}
			else {
				message = "<script>";
				message += "alert('????????? ??????????????????.');";
				message += "location.href = '"+ multipartRequest.getContextPath() + "/inquiry/inquiryList.do';";
				message += "</script>";
			}
			
			
			resEnt = new ResponseEntity(message, responseHeaders, HttpStatus.CREATED);
			
		} catch (Exception e) {
			// ????????? ????????? ?????? temp ????????? ????????? ?????? ??? ???????????? ????????????
			if (imageFileList != null && imageFileList.size() != 0) {
				for (InquiryImgVO inquiryImgVO : imageFileList) {
					imageFileName = inquiryImgVO.getImageFileName();
					
					File srcFile = new File(INQUIRY_IMAGE_REPO + "\\temp\\" + imageFileName);
					srcFile.delete();
				}
			}
			
			message = "<script>";
			message += "alert('????????? ??????????????????.');";
			message += "location.href = '"+ multipartRequest.getContextPath() + "/inquiry/inquiryForm.do';";
			message += "</script>";
			
			resEnt = new ResponseEntity(message, responseHeaders, HttpStatus.CREATED);
			
			e.printStackTrace();
		}
		
		
		return resEnt;
	}
	
	// ?????? ?????? ??? ?????? ????????? ????????? ??????
	private List<String> upload(MultipartHttpServletRequest multipartRequest) throws ServletException, IOException {
		
		List<String> fileList = new ArrayList<>();
		
		Iterator<String> fileNames = multipartRequest.getFileNames();
		while (fileNames.hasNext()) {
			String fileName = fileNames.next();
			
			MultipartFile mFile = multipartRequest.getFile(fileName);
			String originalFileName = mFile.getOriginalFilename();
			
			if (originalFileName != "" && originalFileName != null) {
				fileList.add(originalFileName);
				
				File file = new File(INQUIRY_IMAGE_REPO + "\\" + fileName);
				if (mFile.getSize() != 0) {				// file null check
					if (!file.exists()) {				// ???????????? ???????????? ????????????
						file.getParentFile().mkdirs();	// ????????? ???????????? ?????????????????? ??????
						mFile.transferTo(new File(INQUIRY_IMAGE_REPO + "\\temp\\" + originalFileName));	// ????????? ??????
					}	// ????????? MultiparFile??? ?????? ????????? ??????
				}
			}
			else {
				fileList.add("");
			}
		}
		
		return fileList;
		
	}


	@Override
	@RequestMapping(value = "/inquiry/inquiryVeiw.do", method = {RequestMethod.GET, RequestMethod.POST})
	public ModelAndView inquiryVeiw(@RequestParam("inquiry_cd") String inquiry_cd, 
									@RequestParam(value = "removeCompleted", required = false) String removeCompleted, 
									HttpServletRequest request,
									HttpServletResponse response) throws Exception {

		String viewName = (String) request.getAttribute("viewName");
		
		Map<String, Object> inquiryMap = inquiryService.viewInquiry(inquiry_cd);
		inquiryMap.put("removeCompleted", removeCompleted);
		
		ModelAndView mav = new ModelAndView(viewName);
		mav.addObject("inquiryMap", inquiryMap);
		
		return mav;
	}


	@Override
	@RequestMapping(value = "/inquiry/modifyInquiry.do", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity modifyInquiry(MultipartHttpServletRequest multipartRequest,
										HttpServletResponse response) throws Exception {
			
		multipartRequest.setCharacterEncoding("UTF-8");
		
		Map<String, Object> inquiryMap = new HashMap<>();
		
		Enumeration enu = multipartRequest.getParameterNames();
		while (enu.hasMoreElements()) {
			String name = (String) enu.nextElement();
			
			if (name.equals("inquiry_img_cd")) {
				String[] values = multipartRequest.getParameterValues(name);
				inquiryMap.put(name, values);
			}
			else if (name.equals("oldFileName")) {
				String[] values = multipartRequest.getParameterValues(name);
				inquiryMap.put(name, values);
			}
			else {	// ?????? ????????? ????????? ?????? ??????
				String value = multipartRequest.getParameter(name);
				inquiryMap.put(name, value);
			}
		}

		File temp = new File(INQUIRY_IMAGE_REPO + "\\temp");
		// ????????? ????????? ????????? ????????????
		List<String> fileList = uploadModImageFile(multipartRequest);
		List<InquiryImgVO> modAddImageFileList = new ArrayList<>();
		
		if (fileList != null && fileList.size() != 0) {
			for (int i = 0; i < fileList.size(); i++) {
				String fileName = fileList.get(i);
				if (!fileName.equals("")) {
					InquiryImgVO inquiryImgVO = new InquiryImgVO();

					// ?????? ????????? ???????????? modAddImageFileList??? ?????????
					inquiryImgVO.setImageFileName(fileName);
					modAddImageFileList.add(inquiryImgVO);
					inquiryMap.put("modAddImageFileList", modAddImageFileList);
				}
			}
		}
		
		String inquiry_cd = (String) inquiryMap.get("inquiry_cd");
		String message = "";
		ResponseEntity resEnt = null;
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-type", "text/html; charset=utf-8");
		
		try {
			String inquiry_title = (String) inquiryMap.get("inquiry_title");
			String inquiry_content = (String) inquiryMap.get("inquiry_content");

			System.out.println("!!inquiry_title = " + inquiry_title);
			System.out.println("!!inquiry_content = " + inquiry_content);

			inquiryService.modifyInquiry(inquiryMap);
			
			if (fileList != null && fileList.size() != 0) {	// ????????? ???????????? ???????????? ????????????(temp???????????? ????????????)
				for (int i = 0; i < fileList.size(); i++) {
					String fileName = fileList.get(i);

					if (fileName != null && !fileName.equals("")) {
						// ???????????? ?????? ??????
						File srcFile = new File(INQUIRY_IMAGE_REPO + "\\temp\\" + fileName);
						// ???????????? ??????
						File destDir = new File(INQUIRY_IMAGE_REPO + "\\" + inquiry_cd);
						FileUtils.moveFileToDirectory(srcFile, destDir, true);
					}
				}
			}
			
			message += "<script>";
			message += "alert('????????? ??????????????????.');";
			message += "location.href = '"+ multipartRequest.getContextPath() + "/inquiry/inquiryVeiw.do?inquiry_cd=" + inquiry_cd + "';";
			message += "</script>"; 
			
			resEnt = new ResponseEntity(message, responseHeaders, HttpStatus.CREATED);
			
		} catch (Exception e) {
			
			// ?????? ?????? ??? temp ????????? ???????????? ????????? ???????????? ??????
			File[] file_list = temp.listFiles();
			for (int i = 0; i < file_list.length; i++) {
				file_list[i].delete();
			}
			
			e.printStackTrace();
			
			message = "<script>";
			message += "alert('????????? ??????????????????.');";
			message += "location.href = '"+ multipartRequest.getContextPath() + "/inquiry/inquiryVeiw.do?inquiry_cd=" + inquiry_cd + "';";
			message += "</script>";
			
			resEnt = new ResponseEntity(message, responseHeaders, HttpStatus.CREATED);
		}
		
		return resEnt;
	}
	
	// ????????? ?????? ????????? ???????????????
	private List<String> uploadModImageFile(MultipartHttpServletRequest multipartRequest) throws Exception {
		
		List<String> fileList = new ArrayList<>();
		
		Iterator<String> fileNames = multipartRequest.getFileNames();
		while (fileNames.hasNext()) {
			String fileName = fileNames.next();
			
			MultipartFile mFile = multipartRequest.getFile(fileName);
			String originalFileName = mFile.getOriginalFilename();
			
			if (originalFileName != "" && originalFileName != null) {
				fileList.add(originalFileName);

				System.out.println("!!????????? ?????? ??????1: " + originalFileName);

				File file = new File(INQUIRY_IMAGE_REPO + "\\" + fileName);
				if (mFile.getSize() != 0) {	// File Null Check
					if (!file.exists()) {	// ???????????? ???????????? ?????? ??????
						file.getParentFile().mkdirs();	// ????????? ???????????? ??????????????? ??????
						
						// ?????? ????????? mFile ??????
						mFile.transferTo(new File(INQUIRY_IMAGE_REPO + "\\temp\\" + originalFileName));
					}
				}
			}
			else {	// ????????? ???????????? ?????? ??????
				System.out.println("!!????????? ?????? ??????2: " + originalFileName);

				fileList.add("");
			}
		}
		
		return fileList;
	}


	@Override
	@RequestMapping(value = "/inquiry/removeInquiry.do", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity removeInquiry(@RequestParam("inquiry_cd") String inquiry_cd,
										HttpServletRequest request,
										HttpServletResponse response) throws Exception {

		response.setContentType("text/html; charset=utf-8");
		
		String message = "";
		ResponseEntity resEnt = null;
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-type", "text/html; charset=utf-8");
		
		try {
			inquiryService.removeInquiry(inquiry_cd);	// ???????????? ???????????? ??? ?????????
			
			File destDir = new File(INQUIRY_IMAGE_REPO + "\\" + inquiry_cd);
			FileUtils.deleteDirectory(destDir);		// ????????? ????????? ????????? ????????? ????????? ?????????.
			
			message = "<script>";
			message += "alert('?????? ??????????????????.');";
			message += "location.href = '"+ request.getContextPath() + "/inquiry/inquiryList.do';";
			message += "</script>";
			
			resEnt = new ResponseEntity(message, responseHeaders, HttpStatus.CREATED);
			
		} catch (Exception e) {
			
			message = "<script>";
			message += "alert('?????? ???????????? ??? ????????? ??????????????????. ?????? ????????? ?????????.');";
			message += "location.href = '"+ request.getContextPath() + "/inquiry/inquiryList.do';";
			message += "</script>";
			
			resEnt = new ResponseEntity(message, responseHeaders, HttpStatus.CREATED);
			
			e.printStackTrace();
		}
		
		return resEnt;
	}


	@Override
	@RequestMapping(value = "/inquiry/removeExistImage.do", method = RequestMethod.POST)
	public void removeExistImage(HttpServletRequest request,
							   HttpServletResponse response) throws Exception {

		request.setCharacterEncoding("utf-8");
		response.setContentType("text/html; charset=utf-8");
		PrintWriter out = response.getWriter();
		
		try {
			String inquiry_img_cd = request.getParameter("inquiry_img_cd");
			String inquiry_cd = request.getParameter("inquiry_cd");
			String imageFileName = request.getParameter("imageFileName");
			
			System.out.println("inquiry_img_cd: " + inquiry_img_cd);
			System.out.println("inquiry_cd: " + inquiry_cd);
			
			InquiryImgVO inquiryImgVO = new InquiryImgVO();
			inquiryImgVO.setInquiry_cd(inquiry_cd);
			inquiryImgVO.setInquiry_img_cd(inquiry_img_cd);
			
			inquiryService.removeExistImage(inquiryImgVO);
			
			if (imageFileName != null) {
				
				File removeFile = new File(INQUIRY_IMAGE_REPO + "\\" + inquiry_cd + "\\" + imageFileName);
				removeFile.delete();
			}
			
			out.print("success");
			
			
			
		} catch (Exception e) {
			out.print("fail");
			e.printStackTrace();
		}
		
	}
	
}
