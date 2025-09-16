import { useEffect, useState, useCallback } from "react";
import { Button, Card, Form, Input, List, message, Popconfirm } from "antd";
import { useParams } from "react-router-dom";
import api from "../api/axios";

function MenuPage() {
  const { id } = useParams(); // restaurantId
  const [menus, setMenus] = useState([]);
  const [loading, setLoading] = useState(false);

  // state để sửa menu
  const [editingMenuId, setEditingMenuId] = useState(null);
  const [editingName, setEditingName] = useState("");

  // state thêm menu
  const [newMenuName, setNewMenuName] = useState("");

  const fetchMenus = useCallback(async () => {
    setLoading(true);
    try {
      const res = await api.get(`/restaurants/${id}/menus`);
      setMenus(res.data);
    } catch {
      message.error("Lỗi khi tải menu");
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchMenus();
  }, [fetchMenus]);

  // Xóa menu
  const deleteMenu = async (menuId) => {
    try {
      await api.delete(`/menus/${menuId}`);
      message.success("Xóa menu thành công");
      fetchMenus();
    } catch {
      message.error("Không thể xóa menu");
    }
  };

  // Sửa menu
  const updateMenu = async (menuId) => {
    try {
      await api.put(`/menus/${menuId}`, {
        name: editingName,
        restaurantId: parseInt(id, 10),
      });
      message.success("Cập nhật menu thành công");
      setEditingMenuId(null);
      fetchMenus();
    } catch {
      message.error("Không thể cập nhật menu");
    }
  };

  // Thêm menu
  const addMenu = async () => {
    if (!newMenuName) return message.warning("Vui lòng nhập tên menu");
    try {
      await api.post(`/menus`, {
        name: newMenuName,
        restaurantId: parseInt(id, 10),
      });
      message.success("Thêm menu thành công");
      setNewMenuName("");
      fetchMenus();
    } catch {
      message.error("Không thể thêm menu");
    }
  };

  return (
    <div style={{ padding: 24 }}>
      <h1 className="text-2xl font-bold mb-6">Danh sách Menu</h1>

      <List
        loading={loading}
        dataSource={menus}
        renderItem={(menu) => (
          <Card style={{ marginBottom: 12 }}>
            {editingMenuId === menu.id ? (
              <div style={{ display: "flex", gap: 8 }}>
                <Input
                  value={editingName}
                  onChange={(e) => setEditingName(e.target.value)}
                />
                <Button type="primary" onClick={() => updateMenu(menu.id)}>
                  Lưu
                </Button>
                <Button onClick={() => setEditingMenuId(null)}>Hủy</Button>
              </div>
            ) : (
              <div style={{ display: "flex", justifyContent: "space-between" }}>
                <span>{menu.name}</span>
                <div style={{ display: "flex", gap: 8 }}>
                  <Button
                    onClick={() => {
                      setEditingMenuId(menu.id);
                      setEditingName(menu.name);
                    }}
                  >
                    Sửa
                  </Button>
                  <Popconfirm
                    title="Bạn có chắc muốn xóa menu này?"
                    onConfirm={() => deleteMenu(menu.id)}
                  >
                    <Button danger>Xóa</Button>
                  </Popconfirm>
                </div>
              </div>
            )}
          </Card>
        )}
      />

      {/* Form thêm menu */}
      <Card style={{ marginTop: 24 }}>
        <h2 className="text-xl font-semibold mb-4">Thêm Menu mới</h2>
        <Form layout="inline" onFinish={addMenu}>
          <Form.Item>
            <Input
              placeholder="Tên menu"
              value={newMenuName}
              onChange={(e) => setNewMenuName(e.target.value)}
            />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit">
              Thêm
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
}

export default MenuPage;
