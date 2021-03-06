/* autocomplete */
$(function() {
	var category = [ 
			"Node.js",
			"React",
			"Spring",
			"Java",
			"HTML/CSS",
			"JavaScript",
			"웹디자인",
			"Python",
			"OpenAPI",
			"앱 디자인",
			"안드로이드",
			"IOS",
			"딥러닝",
			"머신러닝",
			"인공지능",
			"Tensorflow",
			"인공신경망",
			"Python 문법",
			"Java 문법",
			"Kotlin 문법",
			"JavaScript 문법",
			"C/C++문법",
			"코딩테스트",
			"문제해결",
			"알고리즘 분석",
			"알고리즘 설계",
			"악성코드",
			"웹방화벽",
			"네트워크",
			"정보보안점검",
			"모의해킹",
			"DB 설계",
			"쿼리 디버그",
			"쿼리 분석",
			"Oracle",
			"DBMS",
			"PT",
			"필라테스",
			"요가",
			"크로스핏",
			"맨몸 운동",
			"운동치료",
			"스포츠마사지",
			"물리치료",
			"도수치료",
			"미술치료",
			"음악치료",
			"명상최면치료",
			"학습치료",
			"놀이치료",
			"언어치료",
			"연극치료",
			"피부관리",
			"탈모관리",
			"두피/모발관리",
			"얼굴경락관리",
			"네일왁싱",
			"헤어관리",
			"영어 회화",
			"중국어 회화",
			"일본어 회화",
			"스페인어 회화",
			"프랑스어 회화",
			"러시아어 회화",
			"아랍어 회화",
			"경영/회계/사무",
			"문화/예술/디자인",
			"음식/서비스",
			"토목",
			"안전관리",
			"환경/에너지",
			"건설기계운전",
			"기계",
			"화학",
			"전기/전자",
			"정보통신",
			"생산관리",
			"기타 자격증",
			"국어 과외",
			"수학 과외",
			"영어 과외",
			"사회 과외",
			"과학 과외",
			"인물논술",
			"수리논술",
			"과학논술",
			"편입논술",
			"피아노",
			"바이올린",
			"비올라",
			"첼로",
			"기타",
			"드럼",
			"마림바",
			"카혼",
			"잼베",
			"보컬",
			"작사",
			"작곡/편곡",
			"랩",
			"성악",
			"코레오",
			"팝핀",
			"락킹",
			"왁킹",
			"브레이킹",
			"크럼프",
			"하우스",
			"한국무용",
			"현대무용",
			"발레",
			"댄스스포츠",
			"방송댄스",
			"인물",
			"풍경",
			"AfterEffect",
			"PremierePro",
			"Lightroom",
			"Cinema 4D",
			"Photoshop",
			"모션그래픽",
			"영상촬영",
			"FinalCutPro",
			"국내주식",
			"해외주식",
			"코인",
			"축구",
			"야구",
			"농구",
			"골프",
			"테니스",
			"배드민턴",
			"탁구",
			"수영",
			"태권도",
			"유도",
			"주짓수",
			"아동미술",
			"성인미술",
			"회화",
			"동양화",
			"서양화",
			"조각",
			"소묘/드로잉",
			"포토샵 드로잉"
	];
	
	$("#autocomplete").autocomplete({
		source: category,
		open: function(event, ui){
			
		},
		select: function(event, ui) {
        	var cate = ui.item.label;
        	location.href="http://localhost:8080/develocket/starcategory/starcategory_detail.do?cate_s="+cate;
	       
        },
        focus : function(event, ui) {   //포커스 가면
                return false;			//한글 에러 잡기용도로 사용됨
        },
        minLength: 1,// 최소 글자수
        autoFocus: true, //첫번째 항목 자동 포커스 기본값 false
        classes: {    //잘 모르겠음
        	"ui-autocomplete": "highlight"
        }
	});
});