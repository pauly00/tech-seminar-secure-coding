-- 기술세미나 시큐어코딩 - 초기 데이터
-- INSERT IGNORE로 재실행 시 오류 방지

INSERT IGNORE INTO `user_tb` (`id`, `user_id`, `user_password`, `user_name`, `user_gender`, `user_email`)
VALUES
    (1, 'admin',  '1234', 'admin',  '여자', 'admin@fisa.com'),
    (2, 'alice',  '1234', 'alice',  '여자', 'alice@fisa.com'),
    (3, 'bob',    '1234', 'bob',    '남자', 'bob@fisa.com'),
    (4, 'melody', '1234', 'melody', '남자', 'melody@fisa.com'),
    (5, 'fisa',   '3343', 'fisa',   '남자', 'fisa@fisa.com'),
    (6, 'csdc',   '3343', 'csdc',   '남자', 'csdc@fisa.com');

INSERT IGNORE INTO `user_bbs` (`id`, `bbs_id`, `bbs_title`, `bbs_userId`, `bbs_date`, `bbs_content`, `is_private`)
VALUES
    (1, 1, '수강 일정 변경 관련 문의', 'melody', '2020-11-09 23:19:03',
     '안녕하세요, FISA 담당자 여러분. 신청한 과정의 수강 일정을 변경할 수 있는지 문의드립니다. 개인 사정으로 인해 일정 조정이 필요한 상황입니다.', 0),
    (2, 2, '데모 환경 접속 오류 문의', 'bob', '2021-07-15 16:19:45',
     '안녕하세요. 데모 환경에 접속하려고 하는데 오류가 발생하고 있습니다. 어떻게 해결해야 하는지 안내 부탁드립니다.', 0),
    (3, 3, '강의 자료 다운로드 문제 문의', 'alice', '2021-09-20 08:20:35',
     '안녕하세요, FISA 담당자님. 강의 자료를 다운로드하려고 했는데 오류가 발생합니다. 확인 및 조치 부탁드립니다.', 0),
    (4, 4, '수료증 발급 관련 문의', 'fisa', '2022-03-28 23:02:08',
     '과정을 수료하였는데 수료증 발급 방법에 대해 안내받고 싶습니다. 발급 절차와 소요 기간을 알려주시면 감사하겠습니다.', 0),
    (5, 5, '과정 등록 취소 및 환불 문의', 'csdc', '2023-02-05 11:33:15',
     '개인 사정으로 인해 신청한 과정의 등록을 취소하고 싶습니다. 취소 및 환불 절차에 대해 안내 부탁드립니다.', 0);

INSERT IGNORE INTO `user_bbs_file` (`id`, `bbs_id`, `filename`, `filerealname`)
VALUES
    (1, 2, 'empty.jpg', 'empty.jpg'),
    (2, 3, 'broken.jpg', 'broken.jpg');

INSERT IGNORE INTO `BoxPrices` (`box_size`, `price`)
VALUES (1,3000),(2,3500),(3,4000),(4,4500),(5,5000),(6,5500),(7,6000);
