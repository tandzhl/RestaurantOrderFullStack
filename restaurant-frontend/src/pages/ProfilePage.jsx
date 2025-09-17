import React, { useEffect, useState } from "react";
import { Tabs, Form, Input, Button, message, Alert, Card } from "antd";
import api from "../api/axios";

const { TabPane } = Tabs;

const ProfilePage = () => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(false);

  // lấy user khi load trang
  useEffect(() => {
    api
      .get("/users/me")
      .then((res) => {
        setUser(res.data);
      })
      .catch(() => {
        message.error("Không thể tải thông tin người dùng");
      });
  }, []);

  // cập nhật firstName, lastName, email
  const handleUpdateProfile = async (values) => {
    setLoading(true);
    try {
      await api.put(`/users/${user.id}`, {
        firstName: values.firstName,
        lastName: values.lastName,
        email: values.email,
      });
      message.success("Cập nhật thông tin thành công!");
      setUser({ ...user, ...values });
    } catch (err) {
      message.error("Có lỗi xảy ra khi cập nhật thông tin", err);
    } finally {
      setLoading(false);
    }
  };

  // đổi mật khẩu
  const handleChangePassword = async (values) => {
    if (values.newPassword !== values.confirmPassword) {
      message.error("Mật khẩu xác nhận không khớp!");
      return;
    }
    setLoading(true);
    try {
      await api.put(`/users/${user.id}/change-password`, {
        oldPass: values.oldPassword,
        newPass: values.newPassword,
      });
      message.success("Đổi mật khẩu thành công!");
    } catch (err) {
      message.error(err.response?.data?.message || "Đổi mật khẩu thất bại", err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Card
      title="Trang cá nhân"
      style={{ maxWidth: 600, margin: "30px auto" }}
    >
      <Tabs defaultActiveKey="info">
        {/* Tab thông tin */}
        <TabPane tab="Thông tin" key="info">
          {user ? (
            <Form
              layout="vertical"
              initialValues={{
                firstName: user.firstName,
                lastName: user.lastName,
                email: user.email,
              }}
              onFinish={handleUpdateProfile}
            >
              <Form.Item
                label="First Name"
                name="firstName"
                rules={[{ required: true, message: "Nhập First Name" }]}
              >
                <Input />
              </Form.Item>
              <Form.Item
                label="Last Name"
                name="lastName"
                rules={[{ required: true, message: "Nhập Last Name" }]}
              >
                <Input />
              </Form.Item>
              <Form.Item
                label="Email"
                name="email"
                rules={[
                  { required: true, message: "Nhập Email" },
                  { type: "email", message: "Email không hợp lệ" },
                ]}
              >
                <Input />
              </Form.Item>
              <Form.Item>
                <Button type="primary" htmlType="submit" loading={loading}>
                  Lưu thay đổi
                </Button>
              </Form.Item>
            </Form>
          ) : (
            <Alert message="Đang tải thông tin..." type="info" />
          )}
        </TabPane>

        {/* Tab đổi mật khẩu */}
        <TabPane tab="Đổi mật khẩu" key="password">
          <Form layout="vertical" onFinish={handleChangePassword}>
            <Form.Item
              label="Mật khẩu cũ"
              name="oldPassword"
              rules={[{ required: true, message: "Nhập mật khẩu cũ" }]}
            >
              <Input.Password />
            </Form.Item>
            <Form.Item
              label="Mật khẩu mới"
              name="newPassword"
              rules={[{ required: true, message: "Nhập mật khẩu mới" }]}
            >
              <Input.Password />
            </Form.Item>
            <Form.Item
              label="Xác nhận mật khẩu"
              name="confirmPassword"
              rules={[{ required: true, message: "Xác nhận mật khẩu mới" }]}
            >
              <Input.Password />
            </Form.Item>
            <Form.Item>
              <Button type="primary" htmlType="submit" loading={loading}>
                Đổi mật khẩu
              </Button>
            </Form.Item>
          </Form>
        </TabPane>
      </Tabs>
    </Card>
  );
};

export default ProfilePage;
