package com.ezen.develocket.mypage.controller;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.ezen.develocket.mypage.service.MypageService;
import com.ezen.develocket.mypage.vo.MypageVO;
import com.ezen.develocket.rocketInfo.vo.RocketInfoVO;
import com.ezen.develocket.starInfo.service.StarInfoService;

@Controller("mypageController")
public class MypageControllerImpl implements MypageController{

	@Autowired
	MypageService mypageService;
	
	@Autowired
	StarInfoService starInfoService;
	
	@Autowired
	MypageVO mypageVO;


	private static String ROCKET_INFO_IMAGE_REPO = "C:\\develocket_repo\\rocketInfo";

	
	@Override
	@RequestMapping(value = "/mypage/*View.do", method = RequestMethod.GET)
	public ModelAndView moveMyPage(HttpServletRequest request, HttpServletResponse response, Model model) throws Exception {

		String viewName = (String) request.getAttribute("viewName");
		ModelAndView mav = new ModelAndView(viewName);

		HttpSession session = request.getSession();
		RocketInfoVO rocketInfoVO = (RocketInfoVO) session.getAttribute("rocketInfoVO");
		String rocket_cd = rocketInfoVO.getRocket_cd();

		MypageVO mypageVO = mypageService.selectMyPageVO(rocket_cd);
		session.setAttribute("mypageVO", mypageVO);
		mav.addObject("mypageVO", mypageVO);


		model.addAttribute("rocket_cd", rocket_cd);
		model.addAttribute("mypageVO", mypageVO);
		//
		System.out.println("rocketCode=="+rocket_cd);
		System.out.println(model);
		System.out.println(rocketInfoVO);
		System.out.println(mypageVO);
		System.out.println(session);
		
		return mav;
	}
	
	@Override
	@RequestMapping(value = "/mypage/recheckPassword.do", method = RequestMethod.GET)
	public ModelAndView moverecheckPwd(HttpServletRequest request, 
									   HttpServletResponse response) throws Exception {
		
		String viewName = (String) request.getAttribute("viewName");

		String check = request.getParameter("check");

		ModelAndView mav = new ModelAndView();

		HttpSession session = request.getSession();
		RocketInfoVO rocketInfoVO = (RocketInfoVO) session.getAttribute("rocketInfoVO");
		System.out.println("!!!????????? = " + rocketInfoVO.getPassword());

		if (rocketInfoVO != null) {
			String password = rocketInfoVO.getPassword();
			if (password.equals("Naver")) {
				if (check.equals("rocket")) {
					mav.setViewName("/mypage/modMemberInfo.do");
				}
				else if (check.equals("star")) {
					mav.setViewName("/mypage/modStarInfoForm.do");
				}
			}
			else {
				mav.addObject("check", check);
				mav.setViewName(viewName);
			}
		}
		else {
			mav.addObject("check", check);
			mav.setViewName(viewName);
		}

		return mav;
	}

	//??????????????????-> ?????????????????? ???????????? ??? ???????????? ??????
	@Override
	@ResponseBody
	@RequestMapping(value = "/mypage/rechkPwd.do", method={RequestMethod.POST, RequestMethod.GET})
	public ResponseEntity rechkPwd(@RequestParam(value = "password", required = false) String password,
								   @RequestParam(value = "check", required = false) String check,
								   HttpServletRequest request, HttpServletResponse response) throws Exception {

		HttpSession session = request.getSession();

//         String check = (String) param.get("check");
//         System.out.println(check);

		String message = "";
		ResponseEntity resEnt = null;
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-type", "text/html; charset=utf-8");

		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

		RocketInfoVO rocketInfoVO = (RocketInfoVO) session.getAttribute("rocketInfoVO");

		String rocket_cd = rocketInfoVO.getRocket_cd();
		String id = rocketInfoVO.getId();

		//jsp?????? ?????? ????????????
		////
		System.out.println("==================================================================");
		System.out.println("?????????????????? ?????? ????????? : " + id);
		System.out.println("?????????????????? ?????? ???????????? : " + password);

		mypageVO.setId(id);
		mypageVO.setRocket_cd(rocket_cd);

		mypageVO = mypageService.reCheckPwd(mypageVO);

		try {
			if(encoder.matches(password,mypageVO.getPassword())) {
				System.out.println("???????????????????????? ??????");
				System.out.println(mypageVO);
				System.out.println(mypageVO.getPassword());

				if(check.equals("rocket")) {
					System.out.println("?????? ????????? ??????");
					message += "<script>";
					message += "alert('?????? ?????? ?????? ???????????? ???????????????.');";
					message += "location.href = '" + request.getContextPath() + "/mypage/modMemberInfo.do';";
					message += "</script>";

					resEnt = new ResponseEntity(message, responseHeaders, HttpStatus.CREATED);

				} else if(check.equals("star")) {
					message += "<script>";
					message += "alert('?????? ?????? ?????? ???????????? ???????????????.');";
					message += "location.href = '" + request.getContextPath() + "/mypage/modStarInfoForm.do';";
					message += "</script>";

					resEnt = new ResponseEntity(message, responseHeaders, HttpStatus.CREATED);
				}
				else {
					message += "<script>";
					message += "alert('????????? ???????????????.?????? ??? ?????? ????????? ?????????.');";
					message += "location.href = '" + request.getContextPath() + "/mypage/modStarInfoForm.do';";
					message += "</script>";

					resEnt = new ResponseEntity(message, responseHeaders, HttpStatus.CREATED);
				}


			}
			else {
				System.out.println("??????????????? ???????????? ????????????.");

				message += "<script>";
				message += "alert('??????????????? ???????????? ????????????..');";
				message += "window.history.back();";
				message += "</script>";

				resEnt = new ResponseEntity(message, responseHeaders, HttpStatus.CREATED);

			}
		} catch (Exception e) {
			e.printStackTrace();

			message += "<script>";
			message += "alert('???????????????.');";
			message += "window.history.back();";
			message += "</script>";

			resEnt = new ResponseEntity(message, responseHeaders, HttpStatus.CREATED);
		}


		return resEnt;

	}
	
	@RequestMapping(value = "/mypage/modMemberInfo.do", method = {RequestMethod.POST, RequestMethod.GET})
	public ModelAndView moveModifyMypage(HttpServletRequest request, 
							 HttpServletResponse response) throws Exception {
		
		String viewName = (String) request.getAttribute("viewName");
				
		ModelAndView mav = new ModelAndView(viewName);
		System.out.println("viewName!!!!!!"+viewName);
		return mav;
	}
	
	//???????????? ??????????????????+???????????? ????????? ->???????????? ??? ????????? ????????? ??????
		@Override
		@ResponseBody
		@RequestMapping(value = "/mypage/ModifySuccess.do", method = {RequestMethod.POST, RequestMethod.GET})
		public ResponseEntity ModifyRocketInfo(@RequestParam(value = "password", required = false) String password, 
									 @RequestParam(value = "originPassword", required = false) String originPassword,
									HttpServletRequest request, HttpServletResponse response) throws DataAccessException {
			HttpSession session = request.getSession();
			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
			
			RocketInfoVO rocketInfoVO = (RocketInfoVO) session.getAttribute("rocketInfoVO");
			
			String id = rocketInfoVO.getId();
			String rocket_cd = rocketInfoVO.getRocket_cd();
			
			System.out.println("???????????? ????????? id : " + id);
			System.out.println("???????????? ????????? rocket_cd : " + rocket_cd);
			System.out.println("jsp?????? ????????? ???????????? ?????? ??? ?????? ???????????? : "+originPassword);
			
			System.out.println("jsp?????? ????????? ????????? ??? ???????????? : " + password);
			
			mypageVO.setId(id);
			mypageVO.setRocket_cd(rocket_cd);
			
			mypageVO = mypageService.reCheckPwd(mypageVO);
			
			String message = "";
			ResponseEntity resEnt = null;
			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.add("Content-type", "text/html; charset=utf-8");
			try {
				if(encoder.matches(originPassword,mypageVO.getPassword())) {
					
					String securePw = encoder.encode(password);
					System.out.println("securePw"+securePw);
					mypageVO.setPassword(securePw);
					mypageService.ModifyRocketInfo(mypageVO);
					session.setAttribute("securePw", securePw);
					
					message += "<script>";
					message += "alert('??????????????? ?????????????????????. ?????????????????? ???????????????.');";
					message += "location.href = '" + request.getContextPath() + "/rocketInfo/logout.do';";
					message += "</script>";
					
					resEnt = new ResponseEntity(message, responseHeaders, HttpStatus.CREATED);
				}
				else {
					System.out.println("???????????? ?????????");
					
					message += "<script>";
					message += "alert('??????????????? ???????????? ????????????. ?????? ??????????????????.');";
					message += "window.history.back();";
					message += "</script>";
					
					resEnt = new ResponseEntity(message, responseHeaders, HttpStatus.CREATED);
					
				}
			} catch (Exception e) {
				System.out.println("???????????? ????????? ????????????");
				
				message += "<script>";
				message += "alert('????????? ??????????????????. ?????? ??????????????????.')";
				message += "location.href='"+request.getContextPath() + "/mypage/modMemberInfo.do';";
				message += "</script>";
				
				resEnt = new ResponseEntity(message, responseHeaders, HttpStatus.CREATED);
				
				e.printStackTrace();
			}
			
			return resEnt;

		}

	@Override 
	@RequestMapping(value = "/mypage/DeleteInfo.do")
	public ModelAndView DeleteRocketInfo(HttpServletRequest request, HttpServletResponse response)
			throws DataAccessException {
		HttpSession session =request.getSession();
		mypageVO = (MypageVO) session.getAttribute("mypageVO");

		String rocket_cd = mypageVO.getRocket_cd();
		String id = mypageVO.getId();
		
		System.out.println("id"+id);
		System.out.println("rocket_cd"+rocket_cd);
		
		mypageVO.setRocket_cd(rocket_cd);
		mypageVO.setId(id);
		
		mypageService.DeleteRocketInfo(mypageVO);
		
		String star_cd = starInfoService.matchStarCD(rocket_cd);
		
		if(!star_cd.equals("0")) {
			// star__field_cd ????????????
			List<Integer> star_field_cd = new ArrayList<Integer>();
			star_field_cd = starInfoService.findStarFieldCd(star_cd);
			
			Map<String, List<Integer>> starFieldCdMap = new HashMap<String, List<Integer>>();
			starFieldCdMap.put("star_field_cd", star_field_cd);
			System.out.println("star_field_cd : " + star_field_cd);
			
			// ?????? ????????? ??????
			starInfoService.deleteProfile(starFieldCdMap);
			
			// ?????? ??????
			starInfoService.deleteStar(star_cd);
		}
		
		ModelAndView mav = new ModelAndView();
		mav.setViewName("redirect:/rocketInfo/logout.do");
		
		return mav;
	}

	@Override
	@RequestMapping(value = "/mypage/modifyProfileImg.do", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity modifyProfileImg(MultipartHttpServletRequest multipartRequest,
										   HttpServletResponse response) throws Exception {

		multipartRequest.setCharacterEncoding("UTF-8");

		Map<String, Object> myPageMap = new HashMap<>();

		Enumeration enu = multipartRequest.getParameterNames();
		while (enu.hasMoreElements()) {
			String name = (String) enu.nextElement();

			String value = multipartRequest.getParameter(name);
			myPageMap.put(name, value);
		}

		Map<String, Object> imageFileMap = uploadModImageFile(multipartRequest);
		String profile_img = (String) imageFileMap.get("profile_img");
		String old_profile_img = (String) myPageMap.get("old_profile_img");
		if (profile_img.equals("")) {		// ????????? ????????? ?????? ??????
			if (old_profile_img != null) {	// ?????? ???????????? ?????? ??????
				myPageMap.put("profile_img", old_profile_img);
			}
			else {							// ?????? ???????????? ?????? ??????
				myPageMap.put("profile_img", profile_img);
			}
		}
		else {								// ????????? ????????? ?????? ??????
			myPageMap.put("profile_img", profile_img);
		}

		String rocket_cd = (String) myPageMap.get("rocket_cd");
		String message = "";
		ResponseEntity resEnt = null;
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-type", "text/html; charset=utf-8");

		try {
			mypageService.modifyProfileImage(myPageMap);

			if (!profile_img.equals("")) {
				// ???????????? ?????? ??????
				File srcFile = new File(ROCKET_INFO_IMAGE_REPO + "\\temp\\" + profile_img);
				// ???????????? ??????
				File destDir = new File(ROCKET_INFO_IMAGE_REPO + "\\" + rocket_cd);
				FileUtils.moveFileToDirectory(srcFile, destDir, true);

				if (old_profile_img != null) {
					File oldFile = new File(ROCKET_INFO_IMAGE_REPO + "\\" + rocket_cd + "\\" + old_profile_img);
					oldFile.delete();
				}
			}

			RocketInfoVO rocketInfoVO = mypageService.selectRocketInfo(rocket_cd);
			HttpSession session = multipartRequest.getSession();
			session.setAttribute("rocketInfoVO", rocketInfoVO);

			message += "<script>";
			message += "alert('????????? ??????????????????.');";
			message += "location.href = '"+ multipartRequest.getContextPath() + "/mypage/myPageView.do';";
			message += "</script>";

			resEnt = new ResponseEntity(message, responseHeaders, HttpStatus.CREATED);

		}
		catch (Exception e) {

			if (profile_img != null && !profile_img.equals("")) {
				File srcFile = new File(ROCKET_INFO_IMAGE_REPO + "\\temp\\" + profile_img);
				srcFile.delete();
			}

			e.printStackTrace();

			message = "<script>";
			message += "alert('????????? ??????????????????.');";
			message += "</script>";

			resEnt = new ResponseEntity(message, responseHeaders, HttpStatus.CREATED);
		}


		return resEnt;
	}

	private Map<String, Object> uploadModImageFile(MultipartHttpServletRequest multipartRequest) throws Exception {

		Map<String, Object> imageFileMap = new HashMap<>();

		Iterator<String> fileNames = multipartRequest.getFileNames();
		while (fileNames.hasNext()) {
			String fileName = fileNames.next();
			MultipartFile mFile = multipartRequest.getFile(fileName);
			String originalFileName = mFile.getOriginalFilename();

			if (originalFileName != "" && originalFileName != null) {
				if (fileName.equals("profile_img")) {
					imageFileMap.put("profile_img", originalFileName);
				}

				File file = new File(ROCKET_INFO_IMAGE_REPO + "\\" + fileName);
				if (mFile.getSize() != 0) {    // File Null Check
					if (!file.exists()) {    // ???????????? ???????????? ?????? ??????
						file.getParentFile().mkdirs();    // ????????? ???????????? ??????????????? ??????

						// ?????? ????????? mFile ??????
						mFile.transferTo(new File(ROCKET_INFO_IMAGE_REPO + "\\temp\\" + originalFileName));
					}
				}
			}
			else {
				if (fileName.equals("profile_img")) {
					imageFileMap.put("profile_img", "");
				}
			}
		}

		return imageFileMap;
	}

	@Override
	@RequestMapping(value = "/mypage/removeExistImage.do", method = RequestMethod.POST)
	public void removeExistImage(@RequestParam(value = "rocket_cd", required = false) String rocket_cd,
								 @RequestParam(value = "profile_img", required = false) String profile_img,
								 HttpServletRequest request,
								 HttpServletResponse response) throws Exception {

		request.setCharacterEncoding("utf-8");
		response.setContentType("text/html; charset=utf-8");
		PrintWriter out = response.getWriter();

		try {
			mypageService.removeExistImage(rocket_cd);
			RocketInfoVO rocketInfoVO = mypageService.selectRocketInfo(rocket_cd);
			HttpSession session = request.getSession();
			session.setAttribute("rocketInfoVO", rocketInfoVO);

			if (profile_img != null) {

				File removeFile = new File(ROCKET_INFO_IMAGE_REPO + "\\" + rocket_cd + "\\" + profile_img);
				removeFile.delete();
			}

			out.print("success");

		} catch (Exception e) {
			out.print("fail");
			e.printStackTrace();
		}
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/mypage/checkStarCD.do", method = RequestMethod.POST)
	public String checkStarCD(HttpServletRequest request, HttpServletResponse response) throws Exception {

		HttpSession session = request.getSession();

		RocketInfoVO rocketInfoVO = (RocketInfoVO) session.getAttribute("rocketInfoVO");
		String rocket_cd = rocketInfoVO.getRocket_cd();
		System.out.println("????????????: " + rocket_cd);

		String result = "";

		String star_cd =  mypageService.checkStarCD(rocket_cd);
		System.out.println("????????????: " + star_cd);
		if (star_cd.equals("0")) {
			result = "empty";
		}
		else {
			result = "exist";
		}

		System.out.println(result);
		return result;
	}
}
