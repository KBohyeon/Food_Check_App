<?php
$servername = "localhost";
$username = "root";
$password = "1234";
$dbname = "foodcheck";

// POST 데이터 받기
$userID = $_POST['userID'];
$foods = json_decode($_POST['foods'], true);

// 데이터베이스 연결
$conn = new mysqli($servername, $username, $password, $dbname);

// 연결 확인
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// 응답 배열 초기화
$response = array();
$response['success'] = true;
$response['message'] = "";

try {
    // 각 음식 항목을 데이터베이스에 저장
    foreach ($foods as $food) {
        $foodName = $food['label'];
        $calories = $food['calories'];
        
        $sql = "INSERT INTO calorie_log (userID, foodName, calories) VALUES (?, ?, ?)";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("ssi", $userID, $foodName, $calories);
        
        if (!$stmt->execute()) {
            throw new Exception("Error: " . $stmt->error);
        }
        
        $stmt->close();
    }
    
    $response['message'] = "음식 정보가 성공적으로 저장되었습니다.";
    
} catch (Exception $e) {
    $response['success'] = false;
    $response['message'] = $e->getMessage();
}

// 연결 종료
$conn->close();

// JSON 응답 반환
header('Content-Type: application/json');
echo json_encode($response);
?>