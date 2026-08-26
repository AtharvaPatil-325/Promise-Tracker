// PromiseTracker Extension Background Service Worker (Manifest V3)

chrome.runtime.onInstalled.addListener(() => {
  chrome.contextMenus.create({
    id: 'create-promise-from-text',
    title: 'Create Promise from "%s"',
    contexts: ['selection'],
  });
});

chrome.contextMenus.onClicked.addListener((info) => {
  if (info.menuItemId === 'create-promise-from-text' && info.selectionText) {
    const selectedText = info.selectionText.trim().substring(0, 5000);

    // Store selected text for the popup to read
    chrome.storage.local.set({ pendingPromiseText: selectedText }, () => {
      // Open extension popup window
      chrome.windows.create({
        url: 'popup.html',
        type: 'popup',
        width: 420,
        height: 600,
      });
    });
  }
});
