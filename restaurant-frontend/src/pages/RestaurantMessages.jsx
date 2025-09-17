import { useEffect, useState, useRef } from "react";
import { useParams } from "react-router-dom";
import { List, Input, Button, Typography, Card } from "antd";
import { SendOutlined } from "@ant-design/icons";
import api from "../api/axios";

const { Title } = Typography;

function RestaurantMessages() {
  const { id } = useParams(); // restaurantId
  const [customers, setCustomers] = useState([]);
  const [selectedUser, setSelectedUser] = useState(null);
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState("");
  const [ownerId, setOwnerId] = useState(null);

  const messagesEndRef = useRef(null);

  // 📌 Lấy thông tin user hiện tại (nhà hàng) từ API
  useEffect(() => {
    const fetchMe = async () => {
      try {
        const res = await api.get("/users/me");
        setOwnerId(res.data.id); // lưu id của nhà hàng
      } catch (err) {
        console.error("Fetch user error:", err);
      }
    };
    fetchMe();
  }, []);

  // 📌 Lấy danh sách hội thoại
  useEffect(() => {
    const fetchCustomers = async () => {
      try {
        const res = await api.get(`/chat/restaurant/${id}/conversations`);
        setCustomers(res.data);
      } catch (err) {
        console.error("Fetch customers error:", err);
      }
    };
    fetchCustomers();
  }, [id]);

  // 📌 Lấy messages khi chọn 1 user
  useEffect(() => {
    if (!selectedUser) return;
    const fetchMessages = async () => {
      try {
        const res = await api.get(
          `/chat/restaurant/${id}/messages/${selectedUser.userId}`
        );
        setMessages(res.data);
      } catch (err) {
        console.error("Fetch messages error:", err);
      }
    };
    fetchMessages();
  }, [id, selectedUser]);

  // 📌 Auto scroll xuống cuối
  useEffect(() => {
    if (messagesEndRef.current) {
      messagesEndRef.current.scrollIntoView({ behavior: "smooth" });
    }
  }, [messages]);

  const sendMessage = async () => {
    if (!newMessage.trim() || !selectedUser) return;

    try {
      await api.post(`/chat/restaurant/send`, null, {
        params: {
          restaurantId: id,
          receiverId: selectedUser.userId,
          message: newMessage,
        },
      });

      setMessages((prev) => [
        ...prev,
        {
          senderId: ownerId,
          senderName: "Nhà hàng",
          receiverId: selectedUser.userId,
          message: newMessage,
          timestamp: Date.now(),
        },
      ]);

      setNewMessage("");
    } catch (err) {
      console.error("Send message error:", err);
    }
  };

  return (
    <div style={{ display: "flex", height: "80vh", padding: 16, gap: 16 }}>
      {/* Sidebar */}
      <Card style={{ width: "250px", overflowY: "auto" }}>
        <Title level={5}>Khách hàng</Title>
        <List
          dataSource={customers}
          renderItem={(conversation) => (
            <List.Item
              onClick={() => setSelectedUser(conversation)}
              style={{
                cursor: "pointer",
                background:
                  selectedUser?.userId === conversation.userId
                    ? "#e6f7ff"
                    : "transparent",
                borderRadius: 8,
                padding: "8px 12px",
              }}
            >
              <div>
                <div style={{ fontWeight: 500 }}>
                  {conversation.fullName}
                </div>
                <div style={{ fontSize: 12, color: "#888" }}>
                  {conversation.lastMessage.message}
                </div>
              </div>
            </List.Item>
          )}
        />
      </Card>

      {/* Khung chat */}
      <Card style={{ flex: 1, display: "flex", flexDirection: "column" }}>
        {selectedUser ? (
          <>
            <Title level={5}>
              Chat với {selectedUser.lastMessage.senderName}
            </Title>
            <div
              style={{
                flex: 1,
                overflowY: "auto",
                marginBottom: 16,
                padding: "8px",
              }}
            >
              {messages.map((msg, index) => {
                const isRestaurant = msg.senderId === ownerId;
                return (
                  <div
                    key={index}
                    style={{
                      display: "flex",
                      justifyContent: isRestaurant ? "flex-end" : "flex-start",
                      marginBottom: 8,
                    }}
                  >
                    <div
                      style={{
                        background: isRestaurant ? "#1677ff" : "#f0f0f0",
                        color: isRestaurant ? "#fff" : "#000",
                        padding: "8px 12px",
                        borderRadius: 16,
                        maxWidth: "60%",
                      }}
                    >
                      {!isRestaurant && (
                        <div style={{ fontSize: 12, marginBottom: 4 }}>
                          {msg.senderName}
                        </div>
                      )}
                      {msg.message}
                    </div>
                  </div>
                );
              })}
              <div ref={messagesEndRef} />
            </div>
            <Input.Group compact style={{ display: "flex" }}>
              <Input
                style={{ flex: 1 }}
                placeholder="Nhập tin nhắn..."
                value={newMessage}
                onChange={(e) => setNewMessage(e.target.value)}
                onPressEnter={sendMessage}
              />
              <Button
                type="primary"
                icon={<SendOutlined />}
                onClick={sendMessage}
              />
            </Input.Group>
          </>
        ) : (
          <div style={{ textAlign: "center", marginTop: "30%" }}>
            <Title level={5}>Chọn một khách để bắt đầu chat</Title>
          </div>
        )}
      </Card>
    </div>
  );
}

export default RestaurantMessages;
