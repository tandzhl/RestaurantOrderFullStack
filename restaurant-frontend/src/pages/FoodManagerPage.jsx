import { useEffect, useState, useCallback, useRef } from "react";
import {
  Button,
  Card,
  Form,
  Input,
  InputNumber,
  List,
  message,
  Popconfirm,
  Upload,
  Select,
} from "antd";
import { useParams } from "react-router-dom";
import { UploadOutlined, PlusOutlined } from "@ant-design/icons";
import api from "../api/axios";

function FoodManagerPage() {
  const { id } = useParams(); // restaurantId
  const [foods, setFoods] = useState([]);
  const [loading, setLoading] = useState(false);
  const [categories, setCategories] = useState([]);
  const [menus, setMenus] = useState([]);

  const [form] = Form.useForm();
  const [editingFoodId, setEditingFoodId] = useState(null);
  const [fileList, setFileList] = useState([]);
  const [showForm, setShowForm] = useState(false); // 👈 điều khiển ẩn/hiện form
  const formRef = useRef(null);

  const fetchFoods = useCallback(async () => {
    setLoading(true);
    try {
      const res = await api.get(`/food-items/restaurant/${id}`);
      setFoods(res.data);
    } catch {
      message.error("Lỗi khi tải món ăn");
    } finally {
      setLoading(false);
    }
  }, [id]);

  const fetchCategories = useCallback(async () => {
    try {
      const res = await api.get(`/categories`);
      setCategories(res.data);
    } catch {
      message.error("Không tải được categories");
    }
  }, []);

  const fetchMenus = useCallback(async () => {
    try {
      const res = await api.get(`/restaurants/${id}/menus`);
      setMenus(res.data);
    } catch {
      message.error("Không tải được menus");
    }
  }, [id]);

  useEffect(() => {
    fetchFoods();
    fetchCategories();
    fetchMenus();
  }, [fetchFoods, fetchCategories, fetchMenus]);

  // Xóa món ăn
  const deleteFood = async (foodId) => {
    try {
      await api.delete(`/food-items/${foodId}`);
      message.success("Xóa món ăn thành công");
      fetchFoods();
    } catch {
      message.error("Không thể xóa món ăn");
    }
  };

  // Submit thêm/sửa
  const onFinish = async (values) => {
    const formData = new FormData();
    formData.append("name", values.name);
    formData.append("price", values.price);
    formData.append("description", values.description || "");
    formData.append("restaurantId", id);
    formData.append("menuId", values.menuId);
    formData.append("categoryId", values.categoryId);

    if (fileList.length > 0 && fileList[0].originFileObj) {
      formData.append("image", fileList[0].originFileObj);
    }

    try {
      if (editingFoodId) {
        await api.put(`/food-items/${editingFoodId}`, formData, {
          headers: { "Content-Type": "multipart/form-data" },
        });
        message.success("Cập nhật món ăn thành công");
      } else {
        await api.post(`/food-items`, formData, {
          headers: { "Content-Type": "multipart/form-data" },
        });
        message.success("Thêm món ăn thành công");
      }
      form.resetFields();
      setEditingFoodId(null);
      setFileList([]);
      setShowForm(false); // ẩn form sau khi lưu
      fetchFoods();
    } catch {
      message.error("Không thể lưu món ăn");
    }
  };

  // Khi ấn sửa
  const onEdit = (food) => {
    setEditingFoodId(food.id);
    form.setFieldsValue({
      name: food.name,
      price: food.price,
      description: food.description,
      menuId: food.menuId || null,
      categoryId: food.categoryId || null,
    });
    setFileList(
      food.imageUrl
        ? [
            {
              uid: "-1",
              name: "image",
              status: "done",
              url: food.imageUrl,
            },
          ]
        : []
    );

    setShowForm(true); // hiện form khi sửa
    setTimeout(() => {
      formRef.current?.scrollIntoView({ behavior: "smooth", block: "start" });
    }, 100);
  };

  return (
    <div style={{ padding: 24 }}>
      <h1 className="text-2xl font-bold mb-6">Quản lý món ăn</h1>
      {/* Nút thêm món ăn */}
      {!showForm && (
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={() => {
            setEditingFoodId(null);
            form.resetFields();
            setFileList([]);
            setShowForm(true);
            setTimeout(() => {
              formRef.current?.scrollIntoView({ behavior: "smooth" });
            }, 100);
          }}
          style={{ marginTop: 16, marginBottom: 16 }}
        >
          Thêm món ăn
        </Button>
      )}

      <List
        loading={loading}
        dataSource={foods}
        renderItem={(food) => (
          <Card style={{ marginBottom: 12 }}>
            <div style={{ display: "flex", gap: 16, alignItems: "center" }}>
              <img
                src={food.imageUrl}
                alt={food.name}
                style={{
                  width: 120,
                  height: 80,
                  objectFit: "cover",
                  borderRadius: 8,
                }}
              />
              <div style={{ flex: 1 }}>
                <h3 className="font-semibold">{food.name}</h3>
                <p>{food.description}</p>
                <p className="text-red-500">
                  {food.price.toLocaleString()} đ
                </p>
              </div>
              <div style={{ display: "flex", gap: 8 }}>
                <Button onClick={() => onEdit(food)}>Sửa</Button>
                <Popconfirm
                  title="Bạn có chắc muốn xóa món này?"
                  onConfirm={() => deleteFood(food.id)}
                >
                  <Button danger>Xóa</Button>
                </Popconfirm>
              </div>
            </div>
          </Card>
        )}
      />

      {/* Form thêm/sửa */}
      {showForm && (
        <Card style={{ marginTop: 24 }} ref={formRef}>
          <h2 className="text-xl font-semibold mb-4">
            {editingFoodId ? "Sửa món ăn" : "Thêm món ăn mới"}
          </h2>
          <Form form={form} layout="vertical" onFinish={onFinish}>
            <Form.Item
              name="name"
              label="Tên món ăn"
              rules={[{ required: true, message: "Nhập tên món ăn" }]}
            >
              <Input />
            </Form.Item>
            <Form.Item
              name="price"
              label="Giá"
              rules={[{ required: true, message: "Nhập giá" }]}
            >
              <InputNumber
                style={{ width: "100%" }}
                min={1000}
                step={1000}
                formatter={(value) =>
                  `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ",")
                }
              />
            </Form.Item>
            <Form.Item name="description" label="Mô tả">
              <Input.TextArea rows={3} />
            </Form.Item>
            <Form.Item
              name="menuId"
              label="Menu"
              rules={[{ required: true, message: "Chọn menu" }]}
            >
              <Select placeholder="Chọn menu">
                {menus.map((menu) => (
                  <Select.Option key={menu.id} value={menu.id}>
                    {menu.name}
                  </Select.Option>
                ))}
              </Select>
            </Form.Item>
            <Form.Item
              name="categoryId"
              label="Category"
              rules={[{ required: true, message: "Chọn category" }]}
            >
              <Select placeholder="Chọn category">
                {categories.map((c) => (
                  <Select.Option key={c.id} value={c.id}>
                    {c.name}
                  </Select.Option>
                ))}
              </Select>
            </Form.Item>
            {/* Upload ảnh */}
            <Form.Item label="Hình ảnh" required={!editingFoodId}>
              <Upload
                fileList={fileList}
                onChange={({ fileList }) => setFileList(fileList)}
                beforeUpload={() => false}
                listType="picture"
                maxCount={1}
              >
                <Button icon={<UploadOutlined />}>Chọn ảnh</Button>
              </Upload>
            </Form.Item>
            <Form.Item>
              <Button type="primary" htmlType="submit">
                {editingFoodId ? "Cập nhật" : "Thêm"}
              </Button>
              <Button
                style={{ marginLeft: 8 }}
                onClick={() => {
                  setEditingFoodId(null);
                  form.resetFields();
                  setFileList([]);
                  setShowForm(false); // ẩn form khi hủy
                }}
              >
                Hủy
              </Button>
            </Form.Item>
          </Form>
        </Card>
      )}
    </div>
  );
}

export default FoodManagerPage;
