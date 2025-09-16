import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Card, Row, Col, Spin, message, Button, Space, Empty } from "antd";
import api from "../api/axios";
import { toUniqueSlug } from "../utils/slug";
const { Meta } = Card;

const CategoryPage = ({ slugMap, categories }) => {
    const { slug } = useParams();
    const navigate = useNavigate();
    const [foodItems, setFoodItems] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (!slugMap || !slugMap[slug]) return;

        const fetchFoodItems = async () => {
            setLoading(true);
            try {
                const categoryId = slugMap[slug];
                const res = await api.get(`/food-items/category/${categoryId}`);
                setFoodItems(res.data.content || []);
            } catch (error) {
                console.error(error);
                message.error("Không thể tải danh sách món ăn!");
            } finally {
                setLoading(false);
            }
        };

        fetchFoodItems();
    }, [slug, slugMap]);

    // Thêm vào giỏ hàng
    const addToCart = (item) => {
        
        console.log("👉 Food item khi add vào giỏ:", item); 
        const cart = JSON.parse(localStorage.getItem("cart")) || [];

        const existing = cart.find((c) => c.id === item.id);
        if (existing) {
            existing.quantity += 1;
        } else {
            cart.push({
                id: item.id,
                name: item.name,
                price: item.price,
                imageUrl: item.imageUrl,
                restaurantId: item.restaurantId,
                quantity: 1,
            });
        }

        localStorage.setItem("cart", JSON.stringify(cart));
        message.success(`${item.name} đã được thêm vào giỏ hàng!`);
    };
    
    const viewDetail = (food) => {
        const slug = `${toUniqueSlug(food.name)}-${food.id}`;
        navigate(`/food/${slug}`);
    };


    if (loading)
        return (
            <Spin
                size="large"
                style={{ marginTop: 50, display: "block", textAlign: "center" }}
            />
        );


    return (
        <div style={{ padding: 24 }}>
            {foodItems.length === 0 ? (
                <Empty description="Không có món ăn nào trong danh mục này" />
            ) : (
                <>
                    <h5 style={{ marginBottom: 24 }}>
                        Danh sách món ăn: {categories?.find((cat) => cat.slug === slug)?.name || ""}
                    </h5>
                    <Row gutter={[24, 24]}>
                        {foodItems.map((item) => (
                            <Col xs={24} sm={12} md={8} lg={6} key={item.id}>
                                <Card
                                    hoverable
                                    cover={
                                        <img
                                            alt={item.name}
                                            src={item.imageUrl}
                                            style={{ height: 180, objectFit: "cover" }}
                                        />
                                    }
                                    actions={[
                                        <div
                                            style={{
                                                display: "flex",
                                                gap: "8px",
                                                justifyContent: "center",
                                            }}
                                        >
                                            <Button type="primary" onClick={() => addToCart(item)}>
                                                Thêm vào giỏ hàng
                                            </Button>
                                            <Button onClick={() => viewDetail(item)}>Xem chi tiết</Button>
                                        </div>,
                                    ]}
                                >
                                    <Meta
                                        title={item.name}
                                        description={
                                            <>
                                                <p>{item.description}</p>
                                                <p style={{ fontWeight: "bold" }}>
                                                    {item.price.toLocaleString("vi-VN")} đ
                                                </p>
                                            </>
                                        }
                                    />
                                </Card>
                            </Col>
                        ))}
                    </Row>
                </>
            )}
        </div>
    );
};

export default CategoryPage;
