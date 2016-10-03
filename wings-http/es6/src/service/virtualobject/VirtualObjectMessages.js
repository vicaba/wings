export function WatchVirtualObject(virtualObjectId) {
  return{
    "op": "vo/watch",
    "path": virtualObjectId
  };
}
