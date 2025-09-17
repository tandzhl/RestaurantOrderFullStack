import React, { useEffect, useState, useCallback } from "react";
import { Table, Button, message, Popconfirm } from "antd";
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
  }, [id, fetchOrders]);

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
      title: "Khách hàng",
      dataIndex: "customerName",
      key: "customerName",
      render: (value) => value || "-", // nếu null thì hiện "-"
    },
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
          <Popconfirm
            title="Xác nhận hủy đơn?"
            description="Bạn có chắc chắn muốn hủy đơn này không?"
            okText="Có"
            cancelText="Không"
            onConfirm={() => handleCancel(record.id)}
          >
            <Button danger size="small">Hủy đơn</Button>
          </Popconfirm>
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
        rowClassName={(record) => {
          if (record.status === "PENDING") return "row-pending";
          if (record.status === "REJECTED" || record.status === "CANCELLED")
            return "row-rejected";
          if (record.status === "SUCCESS") return "row-success";
          return "";
        }}
      />
      <style>
        {`
          .row-pending {
            background-color: #fffbe6 !important; /* vàng nhạt */
          }
          .row-rejected {
            background-color: #fff1f0 !important; /* đỏ nhạt */
          }
          .row-success {
            background-color: #f6ffed !important; /* xanh nhạt */
          }
        `}
      </style>  
    </div>
  );
}
