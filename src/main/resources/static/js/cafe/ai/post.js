document.getElementById('aiPostBtn').addEventListener('click', async function() {
    if (clientValid === "N") {
        const goSetting = confirm("현재 API가 인증되지 않아 카페에 글을 게시할 수 없습니다.\n설정하시겠습니까?");
        if (goSetting) {
            window.location.href = "/myPage/postingSetting";
        }
        return;
    } else if (clientValid === "F") {
        const goSetting = confirm("기존에 인증된 API가 만료되어 카페에 글을 게시하려면 재인증이 필요합니다.\n설정하시겠습니까?");
        if (goSetting) {
            window.location.href = "/myPage/postingSetting";
        }
        return;
    }

    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    const cafeName = usedItem.cafeName;
    const cafeId = selectedCafeIdBtn.dataset.cafeId;

    const cafeBoardTag = usedTagItem.tag;
    const cafeMenuId = selectedTagBtn.dataset.cafeMenuId;

    const prompt = document.getElementById("prompt").value.trim();
    const subject = document.getElementById("subject").value.trim();
    const title = document.getElementById("title").textContent.trim();
    if (title === "") {
        alert("제목을 입력하세요.");
        return;
    }
    const imgUrls = document.getElementById("imgUrls").dataset.value;
    let contentHtml = editor.getHTML();

    var data = {
        cafeName: cafeName,
        cafeId: cafeId,

        cafeBoardTag: cafeBoardTag,
        cafeBoardId: cafeMenuId,

        subject: subject,
        prompt: prompt,
        title: title,
        imgUrls: imgUrls,
        contentHtml: contentHtml
    };

    fetch('/cafe/ai/post', {
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
            alert("카페에 글이 성공적으로 게시 되었습니다.");
            window.location.href = "/cafe/ai/post";
        } else {
            alert("카페 글 게시 요청 중 오류가 발생했습니다. API 설정 확인 및 관리자 문의 바랍니다.");
        }
    })
    .catch(err => {
        console.error(err);
        alert("포스팅 템플릿이 저장 요청 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    });
});

//async function replaceAndRender() {
//
//    let html = editor.getHTML();
//    console.log("html : "+ html);
//    const base64Images = [...html.matchAll(/<img[^>]+src=["'](data:image\/[^"']+)["']/g)]
//                        .map(m => m[1]);
//    console.log("html : "+ html);
//    let replacedHtml = html;
//
//    if (base64Images.length > 0) {
//        const csrfToken = document.querySelector('meta[name="_csrf"]').content;
//        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
//
//        const response = await fetch('/cafe/ai/uploadImages', {
//          method: 'POST',
//          headers: {
//            [csrfHeader]: csrfToken,
//            'Content-Type': 'application/json'
//          },
//          body: JSON.stringify({ base64Images })
//        });
//
//        const urls = await response.json();
//
//        base64Images.forEach((base64, i) => {
//            replacedHtml = replacedHtml.replace(base64, urls[i]);
//        });
//    }
//
//    return replacedHtml;
//}