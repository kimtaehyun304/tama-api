package org.example.tamaapi.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.domain.item.*;
import org.example.tamaapi.domain.order.Delivery;
import org.example.tamaapi.domain.order.Order;
import org.example.tamaapi.domain.order.OrderItem;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class JdbcTemplateRepository {

    private final JdbcTemplate jdbcTemplate;

    public void saveOrders(List<Order> orders) {
        String sql = "INSERT INTO orders (" +
                "member_id, delivery_id, guest_nickname, guest_email, status, " +
                "used_coupon_price, used_point, shipping_fee, payment_id, created_at, updated_at" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Order order = orders.get(i);

                // 1. 회원 주문일 경우
                if (order.getMember() != null) {
                    ps.setLong(1, order.getMember().getId());
                } else {
                    ps.setNull(1, Types.BIGINT);
                }

                // 2. 배송 정보 (null 아니라고 하셨음)
                ps.setLong(2, order.getDelivery().getId());

                // 3~4. Guest 정보가 있을 때만
                if (order.getGuest() != null) {
                    ps.setString(3, order.getGuest().getNickname());
                    ps.setString(4, order.getGuest().getEmail());
                } else {
                    ps.setNull(3, Types.VARCHAR);
                    ps.setNull(4, Types.VARCHAR);
                }

                // 5. 상태 (ENUM → 문자열)
                ps.setString(5, order.getStatus().name());

                // 6. 사용 쿠폰 금액
                ps.setInt(6, order.getUsedCouponPrice());

                // 7. 사용 포인트
                ps.setInt(7, order.getUsedPoint());

                // 8. 배송비
                ps.setInt(8, order.getShippingFee());

                // 9. 결제 번호
                ps.setString(9, order.getPaymentId());

                // 10~11. 생성/수정 시간
                ps.setTimestamp(10, Timestamp.valueOf(order.getCreatedAt()));
                ps.setTimestamp(11, Timestamp.valueOf(order.getUpdatedAt()));
            }

            @Override
            public int getBatchSize() {
                return orders.size();
            }
        });
    }


    public void saveOrderItems(List<OrderItem> orderItems) {
        jdbcTemplate.batchUpdate("INSERT INTO order_item(order_id, color_item_size_stock_id, order_price, count) values (?, ?, ?, ?)", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, orderItems.get(i).getOrder().getId());
                ps.setLong(2, orderItems.get(i).getColorItemSizeStock().getId());
                ps.setInt(3, orderItems.get(i).getOrderPrice());
                ps.setInt(4, orderItems.get(i).getCount());
            }
            @Override
            public int getBatchSize() {
                return orderItems.size();
            }
        });
    }

    public void saveItems(List<Item> items) {

        String sql = """
            INSERT INTO item (
                original_price, now_price, gender, year_season, name, description,
                date_of_manufacture, country_of_manufacture, manufacturer, category_id,
                textile, precaution, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Item item = items.get(i);

                ps.setInt(1, item.getOriginalPrice());
                ps.setInt(2, item.getNowPrice());
                ps.setString(3, item.getGender().name());
                ps.setString(4, item.getYearSeason());
                ps.setString(5, item.getName());
                ps.setString(6, item.getDescription());
                ps.setDate(7, java.sql.Date.valueOf(item.getDateOfManufacture()));
                ps.setString(8, item.getCountryOfManufacture());
                ps.setString(9, item.getManufacturer());
                ps.setObject(10, item.getCategory() != null ? item.getCategory().getId() : null);
                ps.setString(11, item.getTextile());
                ps.setString(12, item.getPrecaution());
                ps.setObject(13, item.getCreatedAt()); // 외부에서 전달받은 값
                ps.setObject(14, item.getCreatedAt()); // 외부에서 전달받은 값
            }

            @Override
            public int getBatchSize() {
                return items.size();
            }
        });



    }


    public void saveColorItems(List<ColorItem> colorItems) {

        jdbcTemplate.batchUpdate("INSERT INTO color_Item(item_id, color_id) values (?, ?)", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, colorItems.get(i).getItem().getId());
                ps.setLong(2, colorItems.get(i).getColor().getId());
            }
            @Override
            public int getBatchSize() {
                return colorItems.size();
            }
        });
    }

    public void saveColorItemSizeStocks(List<ColorItemSizeStock> colorItemSizeStocks) {
        jdbcTemplate.batchUpdate("INSERT INTO color_item_size_stock(color_item_id, size, stock) values (?, ?, ?)", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, colorItemSizeStocks.get(i).getColorItem().getId());
                ps.setString(2, colorItemSizeStocks.get(i).getSize());
                ps.setInt(3, colorItemSizeStocks.get(i).getStock());
            }
            @Override
            public int getBatchSize() {
                return colorItemSizeStocks.size();
            }
        });
    }

    public void saveColorItemImages(List<ColorItemImage> colorItemImages) {

        jdbcTemplate.batchUpdate("INSERT INTO color_item_image(color_item_id, original_file_name, stored_file_name, sequence) values (?, ?, ?, ?)", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, colorItemImages.get(i).getColorItem().getId());
                ps.setString(2, colorItemImages.get(i).getUploadFile().getOriginalFileName());
                ps.setString(3, colorItemImages.get(i).getUploadFile().getStoredFileName());
                ps.setInt(4, colorItemImages.get(i).getSequence());
            }
            @Override
            public int getBatchSize() {
                return colorItemImages.size();
            }
        });
    }

    public void saveDeliveries(List<Delivery> deliveries) {
        String sql = "INSERT INTO delivery (zip_code, street, detail, message, receiver_nickname, receiver_phone, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Delivery delivery = deliveries.get(i);

                ps.setString(1, delivery.getZipCode());
                ps.setString(2, delivery.getStreet());
                ps.setString(3, delivery.getDetail());
                ps.setString(4, delivery.getMessage());
                ps.setString(5, delivery.getReceiverNickname());
                ps.setString(6, delivery.getReceiverPhone());
                ps.setTimestamp(7, Timestamp.valueOf(delivery.getCreatedAt()));
                ps.setTimestamp(8, Timestamp.valueOf(delivery.getUpdatedAt()));
            }

            @Override
            public int getBatchSize() {
                return deliveries.size();
            }
        });
    }

    public void saveReviews(List<Review> reviews) {
        String sql = "INSERT INTO review (order_item_id, member_id, rating, comment, height, weight, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Review review = reviews.get(i);

                ps.setLong(1, review.getOrderItem().getId());   // null 아님
                ps.setLong(2, review.getMember().getId());      // null 아님
                ps.setInt(3, review.getRating());               // 기본값 존재
                ps.setString(4, review.getComment());           // null 허용 시 setObject 사용 가능
                ps.setObject(5, review.getHeight(), java.sql.Types.INTEGER);
                ps.setObject(6, review.getWeight(), java.sql.Types.INTEGER);
                ps.setTimestamp(7, Timestamp.valueOf(review.getCreatedAt())); // BaseEntity에서 상속
                ps.setTimestamp(8, Timestamp.valueOf(review.getUpdatedAt()));
            }

            @Override
            public int getBatchSize() {
                return reviews.size();
            }
        });
    }



}

