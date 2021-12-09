client=3.82.221.209
s1=18.234.167.149
s2=52.90.105.4
s3=54.87.85.89
scp -i ~/.ssh/mysql.pem -r target ubuntu@$client:/home/ubuntu
echo '1st success!'
scp -i ~/.ssh/mysql.pem -r target ubuntu@$s1:/home/ubuntu
echo '2nd success!'
scp -i ~/.ssh/mysql.pem -r target ubuntu@$s2:/home/ubuntu
echo '3rd success!'
scp -i ~/.ssh/mysql.pem -r target ubuntu@$s3:/home/ubuntu
echo '4th success!'
scp -i ~/.ssh/mysql.pem ~/.ssh/mysql.pem ubuntu@$client:/home/ubuntu/.ssh
echo 'upload private key success'
scp -i ~/.ssh/mysql.pem run1.sh ubuntu@$s1:/home/ubuntu
scp -i ~/.ssh/mysql.pem run2.sh ubuntu@$s2:/home/ubuntu
scp -i ~/.ssh/mysql.pem run3.sh ubuntu@$s3:/home/ubuntu
echo 'upload bootstrap shell success'