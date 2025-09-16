import React, { useEffect, useState } from "react";
import { Table, Button, Image, message, Space } from "antd";
import api from "../api/axios";

function PendingShops() {
  const [shops, setShops] = useState([]);
  const [loading, setLoading] = useState(false);

  const fetchShops = async () => {
    try {
      setLoading(true);
      const res = await api.get("http://localhost:8080/admin/restaurants/pending");
      setShops(res.data || []);
    } catch (err) {
      console.error("Lỗi khi fetch pending shops:", err);
      message.error("Không thể tải danh sách cửa hàng!");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchShops();
  }, []);

  const handleApprove = async (id) => {
    try {
      await api.put(`http://localhost:8080/admin/restaurants/${id}/approve`);
      message.success("Đã duyệt cửa hàng!");
      fetchShops();
    } catch (err) {
      console.error("Lỗi khi duyệt:", err);
      message.error("Không thể duyệt cửa hàng!");
    }
  };

  const handleReject = async (id) => {
    try {
      await api.put(`http://localhost:8080/admin/restaurants/${id}/reject`);
      message.success("Đã từ chối cửa hàng!");
      fetchShops();
    } catch (err) {
      console.error("Lỗi khi từ chối:", err);
      message.error("Không thể từ chối cửa hàng!");
    }
  };

  const columns = [
    {
      title: "Ảnh",
      dataIndex: "imageUrl",
      key: "imageUrl",
      render: (url) =>
        url ? (
          <Image src={url} alt="shop" width={80} height={60} style={{ objectFit: "cover" }} />
        ) : (
          <span>Không có ảnh</span>
        ),
    },
    {
      title: "Tên cửa hàng",
      dataIndex: "name",
      key: "name",
    },
    {
      title: "Địa chỉ",
      dataIndex: "address",
      key: "address",
    },
    {
      title: "Giờ mở cửa",
      dataIndex: "openingTime",
      key: "openingTime",
    },
    {
      title: "Giờ đóng cửa",
      dataIndex: "closingTime",
      key: "closingTime",
    },
    {
      title: "Hành động",
      key: "actions",
      render: (_, record) => (
        <Space>
          <Button type="primary" onClick={() => handleApprove(record.id)}>
            Chấp nhận
          </Button>
          <Button danger onClick={() => handleReject(record.id)}>
            Từ chối
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: 20 }}>
      <h2>Danh sách cửa hàng chờ duyệt</h2>
      <Table
        rowKey="id"
        columns={columns}
        dataSource={shops}
        loading={loading}
        pagination={false}
      />
    </div>
  );
}

export default PendingShops;
