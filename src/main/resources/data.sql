-- Tech Seminar Secure Coding - Seed Data
-- INSERT IGNORE prevents errors on re-run

INSERT IGNORE INTO `user_tb` (`id`, `user_id`, `user_password`, `user_name`, `user_gender`, `user_email`)
VALUES
    (1, 'admin',  '1234', 'admin',  '여자', 'admin@hyperjump.com'),
    (2, 'alice',  '1234', 'alice',  '여자', 'alice@hyperjump.com'),
    (3, 'bob',    '1234', 'bob',    '남자', 'bob@hyperjump.com'),
    (4, 'melody', '1234', 'melody', '남자', 'melody@hyperjump.com'),
    (5, 'plass',  '3343', 'plass',  '남자', 'plass@hyperjump.com'),
    (6, 'csdc',   '3343', 'csdc',   '남자', 'csdc@hyperjump.com');

INSERT IGNORE INTO `user_bbs` (`id`, `bbs_id`, `bbs_title`, `bbs_userId`, `bbs_date`, `bbs_content`, `is_private`)
VALUES
    (1, 1, '배송 지연에 대한 궁금증', 'melody', '2020-11-09 23:19:03',
     '안녕하세요, HyperJump Express 고객센터 담당자 여러분. 주문한 제품의 배송이 어떤 이유 때문에 예정일을 넘기고 있는지 궁금합니다.', 0),
    (2, 2, '분실된 택배물 처리에 대한 안내 요청드립니다.', 'bob', '2021-07-15 16:19:45',
     '안녕하세요. 주문한 상품이 분실되어 걱정되고 있습니다. 분실에 대한 조치 및 보상에 대한 안내를 얻을 수 있을까요?', 0),
    (3, 3, '택배 운송 중 상품 파손에 대한 안내 필요', 'alice', '2021-09-20 08:20:35',
     '안녕하세요, HyperJump Express 고객센터 담당자님. 방금 받은 상품이 운송 중에 파손되었다는 알림을 받았습니다.', 0),
    (4, 4, '배송 실패, 주소 수정 및 재배송에 대한 도움이 필요합니다.', 'plass', '2022-03-28 23:02:08',
     '방금 배송 실패 안내를 받았습니다. 주소 수정 및 재배송에 대해 어떻게 진행해야 하는지 자세한 안내 부탁드립니다.', 0),
    (5, 5, '배송 예정 시간 변경 가능 여부 문의', 'csdc', '2023-02-05 11:33:15',
     '주문한 상품의 배송 예정 시간이 제 스케줄과 맞지 않아 변경을 고려 중입니다.', 0);

INSERT IGNORE INTO `user_bbs_file` (`id`, `bbs_id`, `filename`, `filerealname`)
VALUES
    (1, 2, 'empty.jpg', 'empty.jpg'),
    (2, 3, 'broken.jpg', 'broken.jpg');

INSERT IGNORE INTO `BoxPrices` (`box_size`, `price`)
VALUES (1,3000),(2,3500),(3,4000),(4,4500),(5,5000),(6,5500),(7,6000);
