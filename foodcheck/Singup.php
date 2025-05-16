<?php
 
    error_reporting(E_ALL);
    ini_set('display_errors',1);
 
    include('db_conn.php');
 
    $android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");
 
    if( (($_SERVER['REQUEST_METHOD'] == 'POST') && isset($_POST['submit'])) || $android )
    {
        $userID=$_POST['userID'];
        $userPassword=$_POST['userPassword'];
        $email=$_POST['email'];
        $phoneNumber=$_POST['phoneNumber'];
        $userSort=$_POST['userSort'];
 
 
        if(empty($userID)){
            $errMSG = "ID";
        }
        else if(empty($userPassword)){
            $errMSG = "password";
        }
        else if(empty($email)){
            $errMSG = "email";
        }
        else if(empty($phoneNumber)){
            $errMSG = "phonenumber";
        }
        else if(empty($userSort)){
            $errMSG = "sort";
        }
 
        if(!isset($errMSG)){
            try{
                $hashedPassword = sha1($userPassword);
                $stmt = $con->prepare('INSERT INTO  `user`(userID, userPassword, email, phoneNumber, userSort) 
                VALUES(:userID, :userPassword, :email, :phoneNumber, :userSort)');//SHA1 단방향 암호화 처리
    if (!$stmt) {
    die("SQL prepare 실패: " . implode(" / ", $con->errorInfo()));
}
                $stmt->bindValue(':userID', $userID);
                $stmt->bindValue(':userPassword', $hashedPassword); 
                $stmt->bindParam(':email', $email);
                $stmt->bindParam(':phoneNumber', $phoneNumber);
                $stmt->bindParam(':userSort', $userSort);
 
                if($stmt->execute())
                {
                    $successMSG = "SUCCESS";
                }
                else
                {
                    $errMSG = "FAIL";
                }
 
            } catch(PDOException $e) {
                die("Database error: " . $e->getMessage());
            }
        }
 
    }
 
?>
 
<?php
    if (isset($errMSG)) echo $errMSG;
    if (isset($successMSG)) echo $successMSG;
 
        $android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");
 
    if( !$android )
    {
?>
    <html>
       <body>
            <form action="<?php $_PHP_SELF ?>" method="POST">
                ID: <input type = "text" name = "userID" />
                Password: <input type = "text" name = "userPassword" />
                email: <input type = "text" name = "email" />
                phonenumber: <input type = "text" name = "phoneNumber" />
                usersort: <input type = "text" name = "userSort" />
                <input type = "submit" name = "submit" />
            </form>
 
       </body>
    </html>
<?php
    }
?>