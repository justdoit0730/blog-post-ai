document.getElementById('noticeWriteBtn').addEventListener('click', function() {
    const noticeSubject = document.getElementById("selectNoticeSubject").innerText.trim();
    const noticeTitle = document.getElementById('noticeTitle').value.trim();
    const noticeContent = document.getElementById('noticeContent').value.trim();

    if (!noticeTitle) {
        alert('제목을 입력해주세요.');
        return;
    }

    if (!noticeContent) {
        alert('문의 내용을 입력해주세요.');
        return;
    }

    const noticeContentHtml = noticeContent.replace(/\n/g, "<br>");

    var data = {
        noticeSubject: noticeSubject,
        noticeTitle: noticeTitle,
        noticeContent: noticeContentHtml
    };

    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    fetch('/manager/notice/save', {
        method: 'POST',
        headers: {
            [csrfHeader]: csrfToken,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data),
        credentials: 'same-origin'
    })
    .then(response => response.text())
    .then(result => {
        if (result === "T") {
            alert("정상적으로 게시되었습니다.");
            window.location.href = "/etc/community/notice";
        } else {
            alert(result);
            return;
        }
    })
    .catch(err => {
        console.error(err);
        alert("게시글 등록 처리 간 문제가 발생했습니다. 다시 시도해주세요.");
    });
});

document.querySelector("tbody").addEventListener("click", function(e) {
    const btn = e.target.closest(".delete-btn");
    if (!btn) return;

    if (btn.dataset.processing) return;
    btn.dataset.processing = "true";

    const id = btn.dataset.id;
    if (!confirm("정말 삭제하시겠습니까?")) {
        btn.dataset.processing = "";
        return;
    }

    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    fetch('/manager/notice/delete/' + id, {
        method: 'DELETE',
        headers: {
            [csrfHeader]: csrfToken,
            'Content-Type': 'application/json'
        },
        credentials: 'same-origin'
    })
    .then(response => response.text())
    .then(result => {
        if (result === "T") {
            alert("공지사항 게시글이 삭제되었습니다.");
            location.reload();
        } else if (result === "F") {
            window.location.href = "/error";
            btn.dataset.processing = "";
        } else {
            alert("공지사항 게시글 삭제 요청 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            btn.dataset.processing = "";
        }
    })
    .catch(err => {
        alert("글 삭제 요청 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        btn.dataset.processing = "";
    });
});