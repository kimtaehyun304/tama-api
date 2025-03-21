package org.example.tamaapi.repository;

import lombok.RequiredArgsConstructor;
import org.example.tamaapi.domain.OrderItem;
import org.example.tamaapi.domain.item.ColorItem;
import org.example.tamaapi.domain.item.ColorItemImage;
import org.example.tamaapi.domain.item.ColorItemSizeStock;
import org.springdoc.webmvc.core.fn.SpringdocRouteBuilder;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class JdbcTemplateRepository {

    private final JdbcTemplate jdbcTemplate;

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


}

