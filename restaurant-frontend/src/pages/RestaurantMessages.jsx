// src/pages/RestaurantMessages.jsx
import { useEffect, useState, useRef } from "react";
import { useParams } from "react-router-dom";
import { List, Input, Button, Typography, Card, Spin } from "antd";
import { SendOutlined } from "@ant-design/icons";
import api from "../api/axios";
import { db } from "../firebase";
import {
  collection,
  addDoc,
  query,
  where,
  orderBy,
  onSnapshot,
  serverTimestamp,
} from "firebase/firestore";

const { Title } = Typography;

function RestaurantMessages() {
  const { id } = useParams(); // restaurantId
  const [customers, setCustomers] = useState([]);
  const [selectedUser, setSelectedUser] = useState(null);
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState("");
  const [owner, setOwner] = useState(null);
  const [loadingMessages, setLoadingMessages] = useState(false);

  const messagesEndRef = useRef(null);

  // 📌 Lấy thông tin user hiện tại (chủ nhà hàng)
  useEffect(() => {
    const fetchMe = async () => {
      try {
        const res = await api.get("/users/me");
        setOwner(res.data);
      } catch (err) {
        console.error("Fetch user error:", err);
      }
    };
    fetchMe();
  }, []);

  // 📌 Lấy danh sách hội thoại từ backend
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

  // 📌 Lắng nghe messages realtime từ Firestore
  useEffect(() => {
    if (!selectedUser || !owner) return;

    setLoadingMessages(true);

    const q = query(
      collection(db, "messages"),
      where("restaurantId", "==", parseInt(id)),
      orderBy("timestamp", "asc")
    );

    const unsubscribe = onSnapshot(
      q,
      (snapshot) => {
        const msgs = snapshot.docs
          .map((doc) => {
            const data = doc.data() || {};
            let ts = 0;
            const raw = data.timestamp;
            if (raw != null) {
              if (typeof raw === "number") ts = raw;
              else if (raw.toMillis) ts = raw.toMillis();
              else if (raw.seconds) ts = raw.seconds * 1000;
            }
            return {
              id: doc.id,
              ...data,
              timestamp: ts,
            };
          })
          .filter(
            (m) =>
              [owner.id, selectedUser.userId].includes(m.senderId) &&
              [owner.id, selectedUser.userId].includes(m.receiverId)
          )
          .sort((a, b) => a.timestamp - b.timestamp);

        setMessages(msgs);
        setLoadingMessages(false);
      },
      (err) => {
        console.error("Snapshot error:", err);
        setLoadingMessages(false);
      }
    );

    return () => unsubscribe();
  }, [id, selectedUser, owner]);

  // 📌 Auto scroll xuống cuối
  useEffect(() => {
    if (messagesEndRef.current) {
      messagesEndRef.current.scrollIntoView({ behavior: "smooth" });
    }
  }, [messages]);

  const sendMessage = async () => {
    if (!newMessage.trim() || !selectedUser || !owner) return;

    try {
      await addDoc(collection(db, "messages"), {
        senderId: owner.id,
        senderName: `${owner.firstName} ${owner.lastName}`,
        receiverId: selectedUser.userId,
        restaurantId: parseInt(id),
        message: newMessage.trim(),
        timestamp: serverTimestamp(),
      });

      setNewMessage("");
    } catch (err) {
      console.error("Send message error:", err);
    }
  };

  return (
    <div style={{ display: "flex", height: "80vh", padding: 16, gap: 16 }}>
      {/* Sidebar */}
      <Card style={{ width: 250, overflowY: "auto" }}>
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
                <div style={{ fontWeight: 500 }}>{conversation.fullName}</div>
                <div style={{ fontSize: 12, color: "#888" }}>
                  {conversation.lastMessage?.message}
                </div>
              </div>
            </List.Item>
          )}
        />
      </Card>

      {/* Chat box */}
      <Card style={{ flex: 1, display: "flex", flexDirection: "column" }}>
        {selectedUser ? (
          <>
            <Title level={5}>Chat với {selectedUser.fullName}</Title>
            <div
              style={{
                flex: 1,
                overflowY: "auto",
                marginBottom: 16,
                padding: "8px",
                border: "1px solid #eee",
                borderRadius: 8,
              }}
            >
              {loadingMessages ? (
                <div style={{ textAlign: "center", marginTop: 40 }}>
                  <Spin />
                </div>
              ) : messages.length === 0 ? (
                <div style={{ textAlign: "center", color: "#888" }}>
                  Chưa có tin nhắn
                </div>
              ) : (
                messages.map((msg) => {
                  const isRestaurant = msg.senderId === owner?.id;
                  return (
                    <div
                      key={msg.id}
                      style={{
                        display: "flex",
                        justifyContent: isRestaurant
                          ? "flex-end"
                          : "flex-start",
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
                          wordBreak: "break-word",
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
                })
              )}
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
