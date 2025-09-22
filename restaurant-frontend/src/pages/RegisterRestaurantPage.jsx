import React, { useState } from "react";
import { Form, Input, Button, Card, Typography, Steps, TimePicker, Upload, message } from "antd";
import { PlusOutlined } from "@ant-design/icons";
import dayjs from "dayjs";
import api from "../api/axios";
import "../css/RegisterPage.css";

const { Title } = Typography;
const { Step } = Steps;

function RegisterRestaurantPage() {
  const [form] = Form.useForm();
  const [current, setCurrent] = useState(0);
  const [fileList, setFileList] = useState([]);

  const steps = [
    {
      title: "Thông tin",
      content: (
        <>
          <Form.Item
            label="Tên nhà hàng"
            name="name"
            rules={[{ required: true, message: "Vui lòng nhập tên nhà hàng!" }]}
          >
            <Input />
          </Form.Item>
          <Form.Item
            label="Địa chỉ"
            name="address"
            rules={[{ required: true, message: "Vui lòng nhập địa chỉ!" }]}
          >
            <Input />
          </Form.Item>
        </>
      ),
    },
    {
      title: "Hình ảnh",
      content: (
        <Form.Item
          label="Ảnh đại diện"
          name="image"
          valuePropName="fileList"
          getValueFromEvent={(e) => (Array.isArray(e) ? e : e && e.fileList)}
          rules={[{ required: true, message: "Vui lòng chọn ảnh!" }]}
        >
          <Upload
            beforeUpload={() => false}
            maxCount={1}
            listType="picture-card"
            fileList={fileList}
            onChange={({ fileList }) => setFileList(fileList)}
          >
            {fileList.length < 1 && (
              <div>
                <PlusOutlined />
                <div style={{ marginTop: 8 }}>Chọn ảnh</div>
              </div>
            )}
          </Upload>
        </Form.Item>
      ),
    },
    {
      title: "Giờ hoạt động",
      content: (
        <>
          <Form.Item
            label="Giờ mở cửa"
            name="openingTime"
            rules={[{ required: true, message: "Vui lòng chọn giờ mở cửa!" }]}
          >
            <TimePicker format="HH:mm" />
          </Form.Item>
          <Form.Item
            label="Giờ đóng cửa"
            name="closingTime"
            rules={[{ required: true, message: "Vui lòng chọn giờ đóng cửa!" }]}
          >
            <TimePicker format="HH:mm" />
          </Form.Item>
        </>
      ),
    },
  ];

  const next = async () => {
    try {
      // validate các field của bước hiện tại
      await form.validateFields();
      setCurrent(current + 1);
    } catch (err) {
      console.log("Validation error:", err);
    }
  };

  const prev = () => setCurrent(current - 1);

  const registerRestaurant = async () => {
    try {
      // Lấy tất cả dữ liệu từ form
      const values = form.getFieldsValue(true); // true để lấy cả nested giá trị
      const payload = new FormData();

      payload.append("name", values.name);
      payload.append("address", values.address);

      if (values.image && values.image.length > 0) {
        payload.append("image", values.image[0].originFileObj);
      }

      payload.append(
        "openingTime",
        dayjs(values.openingTime).format("HH:mm:ss")
      );
      payload.append(
        "closingTime",
        dayjs(values.closingTime).format("HH:mm:ss")
      );

      await api.post("/restaurants/register", payload, {
        headers: { "Content-Type": "multipart/form-data" },
      });

      message.success("Gửi yêu cầu đăng ký thành công! 🎉 Chờ admin duyệt.");
      form.resetFields();
      setFileList([]);
      setCurrent(0);
    } catch (error) {
      message.error("Đăng ký thất bại. Vui lòng thử lại!");
      console.error(error);
    }
  };

  return (
    <div className="register-container">
      <Card className="register-card" style={{ width: "70%", margin: "0 auto" }}>
        <Title level={3} className="register-title">Đăng ký cửa hàng</Title>
        <Steps current={current} style={{ marginBottom: 24 }}>
          {steps.map((item) => <Step key={item.title} title={item.title} />)}
        </Steps>

        <Form
          form={form}
          layout="vertical"
          onFinish={registerRestaurant}
          autoComplete="off"
          size="large"
        >
          {steps[current].content}

          <div style={{ marginTop: 24 }}>
            {current > 0 && <Button style={{ marginRight: 8 }} onClick={prev}>Quay lại</Button>}
            {current < steps.length - 1 && <Button type="primary" onClick={next}>Tiếp tục</Button>}
            {current === steps.length - 1 && <Button type="primary" htmlType="submit">Gửi yêu cầu</Button>}
          </div>
        </Form>
      </Card>
    </div>
  );
}

export default RegisterRestaurantPage;
