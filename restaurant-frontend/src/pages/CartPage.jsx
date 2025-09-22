import React, { useEffect, useState } from "react";
import {
  Card,
  Button,
  InputNumber,
  message,
  Modal,
  Radio,
  Space,
} from "antd";
import api from "../api/axios"
import dayjs from "dayjs";
import { useNavigate } from "react-router-dom";
import "../css/CartPage.css";

const CartPage = () => {
  const [cart, setCart] = useState([]);
  const [customerId, setCustomerId] = useState(null);
  const [isCheckoutModalVisible, setIsCheckoutModalVisible] = useState(false);
  const [isCashConfirmVisible, setIsCashConfirmVisible] = useState(false);
  const [paymentMethod, setPaymentMethod] = useState("CASH");
  const [loading, setLoading] = useState(false);
  const [currentOrderGroup, setCurrentOrderGroup] = useState(null);

  const navigate = useNavigate();

  useEffect(() => {
    const savedCart = JSON.parse(localStorage.getItem("cart")) || [];
    setCart(savedCart);

    const fetchUser = async () => {
      try {
        const res = await api.get("/users/me"); // ✅ sử dụng api
        setCustomerId(res.data.id);
      } catch (err) {
        console.error("❌ Không lấy được user:", err);
      }
    };

    fetchUser();
  }, []);

  const updateQuantity = (id, value) => {
    const updatedCart = cart.map((item) =>
      item.id === id ? { ...item, quantity: value } : item
    );
    setCart(updatedCart);
    localStorage.setItem("cart", JSON.stringify(updatedCart));
  };

  const removeItem = (id) => {
    const updatedCart = cart.filter((item) => item.id !== id);
    setCart(updatedCart);
    localStorage.setItem("cart", JSON.stringify(updatedCart));
  };

  const totalPrice = cart.reduce(
    (sum, item) => sum + item.price * item.quantity,
    0
  );

  const handleCheckout = async () => {
    if (!customerId) {
      message.warning("Bạn cần đăng nhập để đặt hàng!");
      return;
    }

    if (cart.length === 0) {
      message.warning("Giỏ hàng trống!");
      return;
    }

    setIsCheckoutModalVisible(true);
  };

  function isRestaurantOpen(openingTime, closingTime) {
    const now = dayjs();
    const open = dayjs(openingTime, "HH:mm:ss");
    const close = dayjs(closingTime, "HH:mm:ss");

    if (open.isBefore(close)) {
      return now.isAfter(open) && now.isBefore(close);
    } else {
      return now.isAfter(open) || now.isBefore(close);
    }
  }

  const confirmCheckout = async () => {
    setLoading(true);

    try {
      const restaurantIds = [...new Set(cart.map((item) => item.restaurantId))];

      for (let id of restaurantIds) {
        const res = await api.get(`/restaurants/${id}`);
        const r = res.data;

        if (!isRestaurantOpen(r.openingTime, r.closingTime)) {
          Modal.warning({
            title: "Cửa hàng tạm đóng",
            content: `Nhà hàng "${r.name}" chỉ hoạt động từ ${r.openingTime} đến ${r.closingTime}. Vui lòng đặt hàng trong thời gian này.`,
          });
          setLoading(false);
          return;
        }
      }

      const body = {
        customerId,
        payment: paymentMethod,
        items: cart.map((i) => ({
          foodItemId: i.id,
          restaurantId: i.restaurantId,
          quantity: i.quantity,
        })),
      };

      const res = await api.post("/checkout", body);

      setCurrentOrderGroup(res.data);

      if (paymentMethod === "VNPAY") {
        const payRes = await api.get(`/payment/create-payment?orderId=${res.data.id}`);

        if (payRes.data?.url) {
          setCart([]);
          localStorage.removeItem("cart");

          // 👉 Dùng navigate sang payment-result, không dùng window.location.href
          window.location.href = payRes.data.url;
        } else {
          message.error("Không tạo được link thanh toán VNPay");
          navigate(
            `/payment-result?status=FAILED&orderId=${res.data.id}&amount=0`
          );
        }
      } else {
        setIsCashConfirmVisible(true);
      }

      setIsCheckoutModalVisible(false);
    } catch (error) {
      console.error("❌ Lỗi khi checkout:", error);
      message.error("Có lỗi khi thanh toán!");
    } finally {
      setLoading(false);
    }
  };

  const handleConfirmCash = async () => {
    if (!currentOrderGroup) return;

    try {
      const res = await api.post(
        `/order-groups/${currentOrderGroup.id}/pay`
      );

      message.success("🎉 Đặt hàng thành công!");
      setCart([]);
      localStorage.removeItem("cart");
      setIsCashConfirmVisible(false);

      navigate(
        `/payment-result?status=SUCCESS&orderId=${res.data.id}&amount=${res.data.totalAmount}`
      );
    } catch (err) {
      console.error("❌ Lỗi khi xác nhận COD:", err);
      message.error("Xác nhận thanh toán thất bại!");

      navigate(
        `/payment-result?status=FAILED&orderId=${currentOrderGroup.id}&amount=0`
      );
    }
  };

  return (
    <div className="cart-container">
      <h2 className="cart-title">Giỏ Hàng</h2>
      {cart.length === 0 ? (
        <p className="empty-cart">Giỏ hàng trống</p>
      ) : (
        <>
          {cart.map((item) => (
            <Card key={item.id} className="cart-card">
              <div className="cart-item">
                <img
                  src={item.imageUrl}
                  alt={item.name}
                  className="cart-item-image"
                />

                <div className="cart-item-info">
                  <div className="cart-item-name">{item.name}</div>
                </div>

                <div>
                  <InputNumber
                    min={1}
                    value={item.quantity}
                    onChange={(value) => updateQuantity(item.id, value)}
                  />
                </div>

                <div className="cart-item-price">
                  {(item.price * item.quantity).toLocaleString("vi-VN")} VND
                </div>

                <Button
                  danger
                  onClick={() => removeItem(item.id)}
                  className="cart-item-remove"
                >
                  Xóa
                </Button>
              </div>
            </Card>
          ))}

          <div className="cart-summary">
            <h3>Tổng cộng: {totalPrice.toLocaleString("vi-VN")} VND</h3>
            <Button type="primary" onClick={handleCheckout}>
              Đặt hàng!
            </Button>
          </div>
        </>
      )}

      {/* Modal chọn phương thức thanh toán */}
      <Modal
        title="Xác nhận thanh toán"
        open={isCheckoutModalVisible}
        onOk={confirmCheckout}
        onCancel={() => setIsCheckoutModalVisible(false)}
        okText="Đặt hàng"
        confirmLoading={loading}
      >
        <p>Chọn phương thức thanh toán:</p>
        <Radio.Group
          onChange={(e) => setPaymentMethod(e.target.value)}
          value={paymentMethod}
        >
          <Space direction="vertical">
            <Radio value="CASH">Thanh toán khi nhận hàng</Radio>
            <Radio value="VNPAY">Thanh toán qua VNPay</Radio>
          </Space>
        </Radio.Group>
      </Modal>

      {/* Modal xác nhận COD */}
      <Modal
        title="Xác nhận đặt hàng"
        open={isCashConfirmVisible}
        onOk={handleConfirmCash}
        onCancel={() => setIsCashConfirmVisible(false)}
        okText="Xác nhận"
        cancelText="Hủy"
      >
        <p>Bạn có chắc muốn xác nhận đơn hàng này không?</p>
      </Modal>
    </div>
  );
};

export default CartPage;
