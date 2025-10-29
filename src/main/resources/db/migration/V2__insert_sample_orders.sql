-- Insert 10 orders
-- Insert 10 sample orders
INSERT INTO orders (customer_name, status, total_amount, created_at, updated_at) VALUES
('Ravi Kumar', 'PENDING', 1500.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Priya Sharma', 'PENDING', 2400.50, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Amit Verma', 'PROCESSING', 3200.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Sneha Gupta', 'DELIVERED', 4100.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Rahul Mehta', 'PENDING', 1800.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Neha Agarwal', 'CANCELLED', 2700.75, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Vikas Yadav', 'DELIVERED', 3500.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Meena Reddy', 'PROCESSING', 5000.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Ankit Singh', 'PENDING', 600.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Sonal Jain', 'DELIVERED', 999.99, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


-- Insert corresponding items
INSERT INTO order_items (order_id, sku, name, quantity, price) VALUES
(1, 'LAP123', 'Laptop', 1, 75000.00),
(1, 'MOU456', 'Mouse', 2, 1500.00),
(2, 'TV999', 'Smart TV', 1, 55000.00),
(3, 'PHN101', 'Mobile', 1, 92000.00),
(4, 'BOK555', 'Book Set', 4, 8000.00),
(5, 'FRG222', 'Fridge', 1, 66000.00),
(6, 'WM777', 'Washing Machine', 1, 41000.00),
(7, 'CAM333', 'Camera', 1, 85000.00),
(8, 'TAB444', 'Tablet', 1, 72000.00),
(9, 'DSK999', 'Desk Chair', 1, 99000.00),
(10, 'HD999', 'Hard Disk', 2, 27000.00);
