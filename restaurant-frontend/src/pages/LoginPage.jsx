import { useNavigate } from "react-router-dom";
import { Form, Input, Button, Typography, Card, message } from "antd";
import api from "../api/axios";
import "../css/LoginPage.css";

const { Title, Text } = Typography;

const LoginPage = () => {
  const navigate = useNavigate();

  const handleSubmit = async (values) => {
    try {
      const res = await api.post("/auth/login", values);
      sessionStorage.setItem("token", res.data.accessToken);
      message.success("Đăng nhập thành công!");
      navigate("/");
    } catch (err) {
      console.error("Login error:", err);

      if (err.response) {
        const msg = err.response.data?.message;

        if (msg === "USERNAME_NOT_FOUND") {
          message.error("❌ Tên đăng nhập không tồn tại!");
        } else if (msg === "INVALID_PASSWORD") {
          message.error("❌ Mật khẩu không đúng!");
        } else {
          message.error(msg || "❌ Sai tài khoản hoặc mật khẩu!");
        }
      } else {
        message.error("❌ Không thể kết nối đến server!");
      }
    }
  };

  return (
    <div className="login-container">
      <Card className="login-card">
        {/* Logo */}
        <img
          src="https://res.cloudinary.com/ds4oggqzq/image/upload/v1757120985/2bfb04ad814c4995f0c537c68db5cd0b-multicolor-swirls-circle-logo_boiphx.png"
          alt="logo"
          style={{ width: 80, marginBottom: 16 }}
        />

        <Title level={3}>Đăng nhập</Title>
        <Text type="secondary">Please login to your account</Text>

        {/* Ant Design Form */}
        <Form onFinish={handleSubmit} layout="vertical" style={{ marginTop: 24 }}>
          <Form.Item
            name="username"
            rules={[{ required: true, message: "Vui lòng nhập username!" }]}
          >
            <Input placeholder="Username" />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[{ required: true, message: "Vui lòng nhập password!" }]}
          >
            <Input.Password placeholder="Password" />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" block style={{ borderRadius: 8 }}>
              SIGN IN
            </Button>
          </Form.Item>
        </Form>

        {/* Forgot password */}
        <a href="#" style={{ display: "block", margin: "16px 0" }}>
          Forgot password?
        </a>

        {/* Register */}
        <Text>Don't have an account? </Text>
        <Button type="default" danger style={{ borderRadius: 8 }} onClick={() => navigate("/register")}>
          REGISTER
        </Button>
      </Card>
    </div>
  );
};

export default LoginPage;
