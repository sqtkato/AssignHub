// 削除確認モーダルを開く
function openDeleteModal(accountId) {
    const form = document.getElementById('deleteForm');
    form.action = '/accounts/' + accountId + '/delete';
    const overlay = document.getElementById('deleteOverlay');
    overlay.style.display = 'flex';
}
 
// 削除確認モーダルを閉じる
function closeDeleteModal() {
    document.getElementById('deleteOverlay').style.display = 'none';
}
 
// 背景クリックで削除モーダルを閉じる
document.getElementById('deleteOverlay').addEventListener('click', function(e) {
    if (e.target === this) closeDeleteModal();
});
 
// 一括削除確認モーダルを開く
function openBulkDeleteModal() {
    const checked = document.querySelectorAll('.row-checkbox:checked').length;
    if (checked === 0) {
        document.getElementById('noSelectionOverlay').style.display = 'flex';
        return;
    }
    document.getElementById('bulkDeleteOverlay').style.display = 'flex';
}
 
// 一括削除確認モーダルを閉じる
function closeBulkDeleteModal() {
    document.getElementById('bulkDeleteOverlay').style.display = 'none';
}
 
// 一括削除を実行する
function submitBulkDelete() {
    const form = document.getElementById('listForm');
    form.action = '/accounts/bulk-delete';
    form.method = 'post';
    form.submit();
}
 
// 未選択エラーモーダルを閉じる
function closeNoSelectionModal() {
    document.getElementById('noSelectionOverlay').style.display = 'none';
}
 
// 背景クリックで一括削除モーダルを閉じる
document.getElementById('bulkDeleteOverlay').addEventListener('click', function(e) {
    if (e.target === this) closeBulkDeleteModal();
});
 
// 背景クリックで未選択エラーモーダルを閉じる
document.getElementById('noSelectionOverlay').addEventListener('click', function(e) {
    if (e.target === this) closeNoSelectionModal();
});