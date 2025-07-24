package com.trading.crypto.repository;

import com.trading.crypto.model.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class TransactionRepository implements RepositoryInterface<Transaction, Long>{
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public TransactionRepository(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Long save(Transaction entity) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        int affectedRows = jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO transactions (type, currency, price, quantity, user_id) VALUES(?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS
                );
                ps.setString(1, entity.getType().toString());
                ps.setString(2, entity.getCurrency());
                ps.setBigDecimal(3, entity.getPrice());
                ps.setBigDecimal(4, entity.getQuantity());
                ps.setLong(5, entity.getUser_id());

                return ps;
            }
        }, keyHolder);

        if (affectedRows > 0 && keyHolder.getKey() != null) {
            return keyHolder.getKey().longValue();
        } else {
            throw new RuntimeException("Failed to retrieve generated ID for transaction. Affected rows: " + affectedRows);
        }
    }

    @Override
    public int update(Transaction entity) {
        return jdbcTemplate.update("UPDATE transactions SET type=?, currency=?, price=?, quantity=? WHERE id=?",
                new Object[]{entity.getType(), entity.getCurrency(), entity.getPrice(), entity.getQuantity(), entity.getId()});
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        try {
            Transaction transaction = jdbcTemplate.queryForObject("SELECT * FROM transactions WHERE id=?",
                    BeanPropertyRowMapper.newInstance(Transaction.class), id);

            return Optional.ofNullable(transaction);
        }
        catch (IncorrectResultSizeDataAccessException e){
            return Optional.empty();
        }
    }

    @Override
    public List<Transaction> findAll() {
        return jdbcTemplate.query("SELECT * FROM transactions", BeanPropertyRowMapper.newInstance(Transaction.class));
    }

    @Override
    public int deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM transactions WHERE id=?", id);
    }

    @Override
    public int deleteAll() {
        return jdbcTemplate.update("DELETE FROM transactions");
    }

    public int deleteByUserID(Long userID) {
        return jdbcTemplate.update("DELETE FROM transactions where user_id=?", userID);
    }

    public List<Transaction> findByUserIDOrderByDateAsc(Long userID) {
        return jdbcTemplate.query("SELECT * FROM transactions WHERE user_id = ? ORDER BY date ASC", BeanPropertyRowMapper.newInstance(Transaction.class), userID);
    }
}
