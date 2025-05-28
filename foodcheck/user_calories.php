<?php
// 데이터베이스 연결 정보
	$host = 'localhost';
	$user = 'root';
	$password = '1234';
	$dbname = 'foodcheck';

// 데이터베이스 연결
$conn = new mysqli($servername, $username, $password, $dbname);

// 연결 확인
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// 응답 배열 초기화
$response = array();
$response['success'] = false;

// 요청 메서드 확인
if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    // GET 요청 처리 - 사용자의 권장 칼로리 가져오기
    if (isset($_GET['userID'])) {
        $userID = $_GET['userID'];
        
        // 먼저 user_settings 테이블에서 확인
        $sql = "SELECT recommendedCalories FROM user_settings WHERE userID = ?";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("s", $userID);
        $stmt->execute();
        $result = $stmt->get_result();
        
        if ($result->num_rows > 0) {
            $row = $result->fetch_assoc();
            $response['success'] = true;
            $response['recommendedCalories'] = $row['recommendedCalories'];
        } else {
            // 없으면 user 테이블에서 확인 (칼럼이 있다면)
            $sql = "SELECT recommendedCalories FROM user WHERE userID = ?";
            $stmt = $conn->prepare($sql);
            $stmt->bind_param("s", $userID);
            $stmt->execute();
            $result = $stmt->get_result();
            
            if ($result->num_rows > 0) {
                $row = $result->fetch_assoc();
                $response['success'] = true;
                $response['recommendedCalories'] = $row['recommendedCalories'];
            } else {
                // 기본값 사용
                $response['success'] = true;
                $response['recommendedCalories'] = 2000; // 기본 권장 칼로리
            }
        }
    } else {
        $response['message'] = "userID 매개변수가 필요합니다.";
    }
} elseif ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // POST 요청 처리 - 사용자의 권장 칼로리 설정
    if (isset($_POST['userID']) && isset($_POST['calories'])) {
        $userID = $_POST['userID'];
        $calories = intval($_POST['calories']);
        
        // 값 검증
        if ($calories < 500 || $calories > 5000) {
            $response['message'] = "칼로리는 500에서 5000 사이의 값이어야 합니다.";
        } else {
            // 테이블에 설정 저장/업데이트
            $sql = "INSERT INTO user_settings (userID, recommendedCalories) 
                   VALUES (?, ?) 
                   ON DUPLICATE KEY UPDATE recommendedCalories = ?";
            $stmt = $conn->prepare($sql);
            $stmt->bind_param("sii", $userID, $calories, $calories);
            
            if ($stmt->execute()) {
                $response['success'] = true;
                $response['message'] = "권장 칼로리가 성공적으로 설정되었습니다.";
                $response['recommendedCalories'] = $calories;
            } else {
                $response['message'] = "저장 중 오류가 발생했습니다: " . $stmt->error;
            }
        }
    } else {
        $response['message'] = "userID와 calories 매개변수가 필요합니다.";
    }
} else {
    $response['message'] = "지원되지 않는 요청 메서드입니다.";
}

// 연결 종료
$conn->close();

// JSON 응답 반환
header('Content-Type: application/json');
echo json_encode($response);
?>