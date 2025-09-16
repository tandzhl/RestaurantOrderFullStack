import React, { useEffect, useState, useCallback } from "react";
import { Table, Button, message } from "antd";
import { useParams } from "react-router-dom";
import api from "../api/axios";

export default function RestaurantOrdersPage() {
  const { id } = useParams();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 20,
    total: 0,
  });

  const fetchOrders = useCallback(
    (page = 1, pageSize = 20) => {
      setLoading(true);
      api
        .get(`/orders/restaurant/${id}`, {
          params: {
            page: page - 1, // backend bắt đầu từ 0
            size: pageSize,
          },
        })
        .then((res) => {
          setOrders(res.data.content);
          setPagination({
            current: res.data.pageable.pageNumber + 1,
            pageSize: res.data.pageable.pageSize,
            total: res.data.totalElements,
          });
        })
        .catch((err) => console.error("❌ Lỗi lấy orders:", err))
        .finally(() => setLoading(false));
    },
    [id]
  );

  useEffect(() => {
    fetchOrders(1, pagination.pageSize); // load khi vào page
  }, [id, fetchOrders]); // chỉ chạy khi id thay đổi

  const handleCancel = (orderId) => {
    api
      .post(`/orders/${orderId}/cancel`)
      .then(() => {
        message.success("Hủy đơn thành công!");
        fetchOrders(pagination.current, pagination.pageSize);
      })
      .catch(() => message.error("Hủy đơn thất bại!"));
  };

  const handleTableChange = (pag) => {
    fetchOrders(pag.current, pag.pageSize);
  };

  const columns = [
    { title: "ID", dataIndex: "id", key: "id" },
    {
      title: "Tổng tiền",
      dataIndex: "totalAmount",
      key: "totalAmount",
      render: (value) => `${value.toLocaleString()} VND`,
    },
    { title: "Trạng thái", dataIndex: "status", key: "status" },
    { title: "Thanh toán", dataIndex: "payment", key: "payment" },
    {
      title: "Ngày tạo",
      dataIndex: "createdAt",
      key: "createdAt",
      render: (value) => (value ? new Date(value).toLocaleString() : "-"),
    },
    {
      title: "Hành động",
      key: "action",
      render: (_, record) =>
        record.status === "PENDING" ? (
          <Button danger size="small" onClick={() => handleCancel(record.id)}>
            Hủy đơn
          </Button>
        ) : null,
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      <h1 style={{ fontSize: 24, fontWeight: "bold", marginBottom: 16 }}>
        Đơn hàng của nhà hàng:
      </h1>
      <Table
        dataSource={orders}
        columns={columns}
        rowKey="id"
        loading={loading}
        pagination={pagination}
        onChange={handleTableChange}
        bordered
      />
    </div>
  );
}
