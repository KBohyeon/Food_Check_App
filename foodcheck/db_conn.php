<?php
	
	
	// Connecting DB
	$host = 'localhost';
	$user = 'root';
	$password = '1234';
	$dbname = 'foodcheck';
	
	// try {
	// $con = mysqli_connect($host, $user, $password,$dbname);
	// } catch (PDOException $e) {
    // die("DB 연결 실패: " . $e->getMessage());

	try {
    $con = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8", $user, $password);
    $con->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
} catch (PDOException $e) {
    die("DB 연결 실패: " . $e->getMessage());
}

?>