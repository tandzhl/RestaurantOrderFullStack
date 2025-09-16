import { useEffect, useState } from "react";
import { Form, Input, Button, TimePicker, Upload, message, Card } from "antd";
import { UploadOutlined } from "@ant-design/icons";
import dayjs from "dayjs";
import api from "../api/axios";
import { useParams, useNavigate } from "react-router-dom";

function RestaurantEditPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [fileList, setFileList] = useState([]);

  useEffect(() => {
    // Lấy thông tin nhà hàng hiện tại để fill form
    api
      .get(`/restaurants/${id}`)
      .then((res) => {
        const r = res.data;
        form.setFieldsValue({
          name: r.name,
          address: r.address,
          openingTime: r.openingTime ? dayjs(r.openingTime, "HH:mm:ss") : null,
          closingTime: r.closingTime ? dayjs(r.closingTime, "HH:mm:ss") : null,
        });

        if (r.imageUrl) {
          setFileList([
            {
              uid: "-1",
              name: "current-image",
              status: "done",
              url: r.imageUrl,
            },
          ]);
        }
      })
      .catch((err) => {
        console.error("❌ Lỗi load nhà hàng:", err);
        message.error("Không tải được thông tin nhà hàng");
      });
  }, [id, form]);

  const handleFinish = async (values) => {
    setLoading(true);
    try {
      const formData = new FormData();
      if (values.name) formData.append("name", values.name);
      if (values.address) formData.append("address", values.address);
      if (values.openingTime)
        formData.append("openingTime", values.openingTime.format("HH:mm:ss"));
      if (values.closingTime)
        formData.append("closingTime", values.closingTime.format("HH:mm:ss"));
      if (fileList.length > 0 && fileList[0].originFileObj) {
        formData.append("image", fileList[0].originFileObj);
      }

      await api.put(`/restaurants/${id}`, formData, {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      });

      message.success("Cập nhật nhà hàng thành công!");
      navigate("/owner/dashboard");
    } catch (err) {
      console.error("❌ Lỗi cập nhật:", err);
      message.error("Cập nhật thất bại, vui lòng thử lại!");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: 24, background: "#f5f5f5", minHeight: "100vh" }}>
      <Card
        title="Chỉnh sửa thông tin nhà hàng"
        style={{ maxWidth: 600, margin: "0 auto" }}
      >
        <Form form={form} layout="vertical" onFinish={handleFinish}>
          <Form.Item label="Tên nhà hàng" name="name">
            <Input placeholder="Nhập tên nhà hàng" />
          </Form.Item>

          <Form.Item label="Địa chỉ" name="address">
            <Input placeholder="Nhập địa chỉ" />
          </Form.Item>

          <Form.Item label="Giờ mở cửa" name="openingTime">
            <TimePicker format="HH:mm" />
          </Form.Item>

          <Form.Item label="Giờ đóng cửa" name="closingTime">
            <TimePicker format="HH:mm" />
          </Form.Item>

          <Form.Item label="Ảnh nhà hàng">
            <Upload
              fileList={fileList}
              beforeUpload={() => false}
              onChange={({ fileList }) => setFileList(fileList)}
              listType="picture"
              maxCount={1}
            >
              <Button icon={<UploadOutlined />}>Chọn ảnh</Button>
            </Upload>
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loading}>
              Lưu thay đổi
            </Button>
            <Button
              style={{ marginLeft: 8 }}
              onClick={() => navigate("/owner/dashboard")}
            >
              Hủy
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
}

export default RestaurantEditPage;
