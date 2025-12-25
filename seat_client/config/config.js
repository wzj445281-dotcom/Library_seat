// 这里的地址填你后端运行的地址
// 【本地开发】后端默认端口是8080，所以用 http://localhost:8080/api
// 【真机调试】必须使用局域网IP，不能用localhost (如 http://192.168.1.5:8080/api)
// 【Docker部署】后端端口映射为8081，用 http://你的IP:8081/api
// 获取本机IP：Windows用 ipconfig，Mac/Linux用 ifconfig
const BASE_URL = 'http://localhost:8080/api';

export default {
    BASE_URL
}