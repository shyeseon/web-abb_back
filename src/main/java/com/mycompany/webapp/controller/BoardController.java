package com.mycompany.webapp.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mycompany.webapp.dto.Board;
import com.mycompany.webapp.dto.Pager;
import com.mycompany.webapp.service.BoardService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController //@Responsebody 어노테이션 없이도 리턴된 제이슨 객체(배열)가 응답 바디에 알아서 들어감
@RequestMapping("/board")
public class BoardController {
	@Autowired
	private BoardService boardService;
	
	@GetMapping("/list")
	public Map<String, Object> list(@RequestParam(defaultValue = "1") int pageNo) {
		//페이징 대상이 되는 전체 행수 얻기
		int totalRows = boardService.getCount();
		//페이저 객체 생성
		Pager pager = new Pager(10, 5, totalRows, pageNo);
		//해당 페이지의 게시물 목록 가져오기
		List<Board> list = boardService.getList(pager);
		//여러 객체를(list, pager) 리턴하기 위해 Map 객체 생성
		Map<String, Object> map = new HashMap<>();
		map.put("boards", list);
		map.put("pager", pager);
		return map; //{ "boards": [...], "pager": {...} }
		//각 객체는 제이슨 객체의 속성으로 들어가고, 객체의 속성들은 배열 형태로 들어간다
	}
	
	//@Secured("ROLE_USER") //권한 이름(롤 이름)만 줄 수 있음 //현재 버전에서 사용 불가능
	@PreAuthorize("hasAuthority('ROLE_USER')") //권한 이름(롤 이름)뿐만이 아니라 다양한 표현식을 줄 수 있음 //실행 전 권한 설정
	//@PostAuthorize //실행 후 권한 설정
	@PostMapping("/create")
	public Board create(Board board, Authentication authentication) {
		//첨부가 넘어왔을 경우 처리
		if(board.getBattach() != null && !board.getBattach().isEmpty()) {
			MultipartFile mf = board.getBattach();
			//파일 이름을 설정
			board.setBattachoname(mf.getOriginalFilename());
			//파일 종류를 설정
			board.setBattachtype(mf.getContentType());
			try {
				//파일 데이터를 설정
				board.setBattachdata(mf.getBytes());
			} catch (IOException e) {
			}
		}
		//DB에 저장
		board.setBwriter(authentication.getName());
		boardService.insert(board);
		//JSON 으로 변환되지 않는 필드는 null 처리 (multipartFile, binary data(byte[]))
		board.setBattach(null);
		board.setBattachdata(null);
		return board; //{"bno":1, "btitle":"제목", ...}
	}
	
//	@GetMapping("/read") //http://localhost/read?bno=5 -> 쿼리스트링 방식으로 bno를 받음
//	public Board read(int bno) {
//	}
	
	@PreAuthorize("hasAuthority('ROLE_USER')")
	@GetMapping("/read/{bno}") //http://localhost/read/5 -> @PathVariable 방식으로 bno를 받음
	public Board read(@PathVariable int bno) {
		//bno에 해당하는 Board 객체 얻기
		Board board = boardService.getBoard(bno);
		//JSON 으로 변환되지 않는 필드는 null 처리 (multipartFile, binary data(byte[]))
		board.setBattachdata(null);
		return board;
	}
	
	//@Secured("ROLE_USER") //권한 이름(롤 이름)만 줄 수 있음 //현재 버전에서 사용 불가능
	@PreAuthorize("hasAuthority('ROLE_USER')")
	@PutMapping("/update")
	//put 방식 -> @RequestBody 사용해서 업데이트 -> json 객체로 리턴
	public Board update(Board board) {
		//첨부가 넘어왔을 경우 처리
		if(board.getBattach() != null && !board.getBattach().isEmpty()) {
			MultipartFile mf = board.getBattach();
			//파일 이름을 설정
			board.setBattachoname(mf.getOriginalFilename());
			//파일 종류를 설정
			board.setBattachtype(mf.getContentType());
			try {
				//파일 데이터를 설정
				board.setBattachdata(mf.getBytes());
			} catch (IOException e) {
			}
		}
		//수정하기
		boardService.update(board);
		//수정된 내용의 Board 객체 얻기
		board = boardService.getBoard(board.getBno());
		//JSON으로 변환되지 않는 필드는 null 처리
		board.setBattachdata(null);
		return board;
	}
	
	//@Secured("ROLE_USER")  //권한 이름(롤 이름)만 줄 수 있음 //현재 버전에서 사용 불가능
	@PreAuthorize("hasAuthority('ROLE_USER')")
	@DeleteMapping("/delete/{bno}")
	public void delete(@PathVariable int bno) {
		boardService.delete(bno);
	}
	
	@PreAuthorize("hasAuthority('ROLE_USER')")
	@GetMapping("/battach/{bno}")
	public void download(@PathVariable int bno, HttpServletResponse response) {
		//해당 게시물 가져오기
		Board board = boardService.getBoard(bno);
		try {
			//파일 이름이 한글일 경우, 브라우저에서 한글 이름으로 다운로드 받기 위해 헤더에 추가할 내용
			String fileName = new String(board.getBattachoname().getBytes("UTF-8"), "ISO-8859-1");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
			//파일 타입을 헤더에 추가
			response.setContentType(board.getBattachtype());
			//응답 바디에 파일 데이터를 출력
			OutputStream os = response.getOutputStream();
			os.write(board.getBattachdata());
			os.flush();
			os.close();
		//첨부가 없는 경우는 요청 없음
		} catch (IOException e) {
			log.error(e.toString());
		}		
	}
}



//데이터를 전달하는 방식

//get 방식
//1. @PathVariable -> board/3
//2. quarystring -> board?bno=3&btitle="제목"


//post 방식
//1. form-data -> 멀티파트
//2. x-www-form-urlencoded -> 쿼리스트링 board?bno=3&btitle="제목"
//3. raw -> 메소드(@requestbody dto이름 dto객체) -> json 객체로 리턴 





