import React, { useState } from "react";
import { Form, Input, Button, Card, Typography, message, Steps } from "antd";
import { useNavigate } from "react-router-dom";
import api from "../api/axios";

const { Title } = Typography;
const { Step } = Steps;

function RegisterPage() {
  const [form] = Form.useForm();
  const [current, setCurrent] = useState(0);
  const [formData, setFormData] = useState({}); // 👈 state chứa toàn bộ dữ liệu
  const navigate = useNavigate();

  const steps = [
    {
      title: "Họ tên",
      content: (
        <>
          <Form.Item
            label="Họ"
            name="firstName"
            rules={[{ required: true, message: "Vui lòng nhập họ!" }]}
          >
            <Input />
          </Form.Item>
          <Form.Item
            label="Tên"
            name="lastName"
            rules={[{ required: true, message: "Vui lòng nhập tên!" }]}
          >
            <Input />
          </Form.Item>
        </>
      ),
    },
    {
      title: "Email",
      content: (
        <Form.Item
          label="Email"
          name="email"
          rules={[
            { required: true, message: "Vui lòng nhập email!" },
            { type: "email", message: "Email không hợp lệ!" },
          ]}
        >
          <Input />
        </Form.Item>
      ),
    },
    {
      title: "Tài khoản",
      content: (
        <>
          <Form.Item
            label="Tên đăng nhập"
            name="username"
            rules={[{ required: true, message: "Vui lòng nhập tên đăng nhập!" }]}
          >
            <Input />
          </Form.Item>
          <Form.Item
            label="Mật khẩu"
            name="password"
            rules={[{ required: true, message: "Vui lòng nhập mật khẩu!" }]}
            hasFeedback
          >
            <Input.Password />
          </Form.Item>
          <Form.Item
            label="Xác nhận mật khẩu"
            name="confirmPassword"
            dependencies={["password"]}
            hasFeedback
            rules={[
              { required: true, message: "Vui lòng nhập lại mật khẩu!" },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue("password") === value) {
                    return Promise.resolve();
                  }
                  return Promise.reject(new Error("2 mật khẩu không giống nhau"));
                },
              }),
            ]}
          >
            <Input.Password />
          </Form.Item>
        </>
      ),
    },
  ];

  const next = async () => {
    try {
      const values = await form.validateFields();
      setFormData({ ...formData, ...values }); // 👈 merge dữ liệu vào state
      setCurrent(current + 1);
    } catch {
      return;
    }
  };

  const prev = () => {
    setCurrent(current - 1);
  };

  const onFinish = async (values) => {
    const payload = { ...formData, ...values, role: "CUSTOMER" }; // gom tất cả dữ liệu
    console.log("Register payload:", payload);

    try {
      await api.post("/auth/register", payload);
      message.success("Đăng ký thành công! 🎉");
      form.resetFields();
      setFormData({});
      setCurrent(0);

      setTimeout(() => {
        navigate("/login");
      }, 1000);
    } catch (error) {
      if (error.response?.data?.message) {
        message.error(error.response.data.message);
      } else {
        message.error("Đăng ký thất bại. Vui lòng thử lại!");
      }
      console.error("Register error:", error);
    }
  };

  return (
    <div className="register-container">
      <Card className="register-card">
        <Title level={3} className="register-title">
          Đăng ký tài khoản
        </Title>
        <Steps current={current} style={{ marginBottom: 24 }}>
          {steps.map((item) => (
            <Step key={item.title} title={item.title} />
          ))}
        </Steps>

        <Form
          form={form}
          layout="vertical"
          onFinish={onFinish}
          autoComplete="off"
          size="large"
        >
          {steps[current].content}

          <div style={{ marginTop: 24 }}>
            {current > 0 && (
              <Button style={{ marginRight: 8 }} onClick={prev}>
                Quay lại
              </Button>
            )}
            {current < steps.length - 1 && (
              <Button type="primary" onClick={next}>
                Tiếp tục
              </Button>
            )}
            {current === steps.length - 1 && (
              <Button type="primary" htmlType="submit">
                Đăng ký
              </Button>
            )}
          </div>
        </Form>
      </Card>
    </div>
  );
}

export default RegisterPage;
