document.querySelector("tbody").addEventListener("click", function(e) {
    const btn = e.target.closest(".delete-btn");
    if (!btn) return;

    // 이미 처리된 버튼이면 무시
    if (btn.dataset.processing) return;
    btn.dataset.processing = "true"; // 처리 중 표시

    const id = btn.dataset.id;
    if (!confirm("정말 삭제하시겠습니까?")) {
        btn.dataset.processing = ""; // 취소 시 다시 처리 가능
        return;
    }

    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    fetch('/feature/write/delete/' + id, {
        method: 'POST',
        headers: {
            [csrfHeader]: csrfToken,
            'Content-Type': 'application/json'
        },
        credentials: 'same-origin'
    })
    .then(response => response.text())
    .then(result => {
        if (result === "T") {
            alert("글이 삭제 되었습니다.");
            location.reload();
        } else {
            alert("글 삭제 요청 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            btn.dataset.processing = ""; // 실패 시 다시 처리 가능
        }
    })
    .catch(err => {
        alert("글 삭제 요청 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        btn.dataset.processing = ""; // 에러 시 다시 처리 가능
    });
});