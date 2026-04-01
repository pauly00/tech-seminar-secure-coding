document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.attack-hint code').forEach(function (code) {
        var btn = document.createElement('button');
        btn.className = 'copy-btn';
        btn.type = 'button';
        btn.textContent = '복사';
        btn.addEventListener('click', function () {
            navigator.clipboard.writeText(code.textContent).then(function () {
                btn.textContent = '완료';
                btn.classList.add('copied');
                setTimeout(function () {
                    btn.textContent = '복사';
                    btn.classList.remove('copied');
                }, 1500);
            });
        });
        code.parentNode.insertBefore(btn, code.nextSibling);
    });
});
