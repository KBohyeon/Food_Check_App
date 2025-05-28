<?php
// 데이터베이스 연결 정보
$servername = "localhost";
$username = "root";
$password = "1234";
$dbname = "foodcheck";

// 오류 표시 설정
ini_set('display_errors', 1);
error_reporting(E_ALL);

// GET 데이터 받기
$userID = isset($_GET['userID']) ? $_GET['userID'] : '';

// 오늘 날짜 (YYYY-MM-DD 형식)
$date = date('Y-m-d');

// 디버그 로그 (실제 서버에서는 제거)
error_log("요청 받음 - userID: " . $userID . ", date: " . $date);

// 응답 배열 초기화
$response = array();
$response['success'] = false;
$response['message'] = "";
$response['totalCalories'] = 0;
$response['foods'] = array();
$response['debug'] = "userID: " . $userID . ", date: " . $date; // 디버그 정보

// userID 검증
if (empty($userID)) {
    $response['message'] = "사용자 ID가 제공되지 않았습니다.";
    header('Content-Type: application/json');
    echo json_encode($response);
    exit;
}

try {
    // 데이터베이스 연결
    $conn = new mysqli($servername, $username, $password, $dbname);

    // 연결 확인
    if ($conn->connect_error) {
        throw new Exception("데이터베이스 연결 실패: " . $conn->connect_error);
    }

    // 해당 사용자의 오늘 먹은 음식 가져오기
    $sql = "SELECT foodName, calories FROM calorie_log WHERE userID = ? AND logDate = ?";
    $stmt = $conn->prepare($sql);
    
    if (!$stmt) {
        throw new Exception("쿼리 준비 실패: " . $conn->error);
    }
    
    $stmt->bind_param("ss", $userID, $date);
    
    if (!$stmt->execute()) {
        throw new Exception("쿼리 실행 실패: " . $stmt->error);
    }
    
    $result = $stmt->get_result();
    
    if (!$result) {
        throw new Exception("결과 가져오기 실패: " . $stmt->error);
    }
    
    $totalCalories = 0;
    $foodCount = 0;
    
    // 결과 처리
    while ($row = $result->fetch_assoc()) {
        $foodCount++;
        $calories = intval($row['calories']);
        $totalCalories += $calories;
        
        $response['foods'][] = array(
            'name' => $row['foodName'],
            'calories' => $calories
        );
    }
    
    $response['success'] = true;
    $response['totalCalories'] = $totalCalories;
    $response['message'] = "데이터를 성공적으로 가져왔습니다. 음식 항목 수: " . $foodCount;
    
    $stmt->close();
    
    // 만약 음식 데이터가 없다면
    if (empty($response['foods'])) {
        $response['message'] = "오늘 저장된 음식 데이터가 없습니다.";
    }
    
} catch (Exception $e) {
    $response['success'] = false;
    $response['message'] = $e->getMessage();
    error_log("Error: " . $e->getMessage());
} finally {
    // 연결 종료
    if (isset($conn)) {
        $conn->close();
    }
}

// JSON 응답 반환
header('Content-Type: application/json');
echo json_encode($response);
?>