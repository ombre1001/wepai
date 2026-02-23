const gameArea = document.querySelector('.game-area');
const snake = [{ x: 2, y: 2 }]; 

function initSnake() {
  snake.forEach(segment => {
    const snakeSegment = document.createElement('div');
    snakeSegment.classList.add('snake');
    snakeSegment.style.left = segment.x * 20 + 'px';
    snakeSegment.style.top = segment.y * 20 + 'px';
    gameArea.appendChild(snakeSegment);
  });
}

initSnake();


let direction = "right"; 

document.addEventListener("keydown", (event) => {
  if (event.key === "ArrowUp" && direction !== "down") {
    direction = "up";
  } else if (event.key === "ArrowDown" && direction !== "up") {
    direction = "down";
  } else if (event.key === "ArrowLeft" && direction !== "right") {
    direction = "left";
  } else if (event.key === "ArrowRight" && direction !== "left") {
    direction = "right";
  }
});

function moveSnake() {
  const head = { x: snake[0].x, y: snake[0].y };
  console.log(direction);
  if (direction === "up") {
    head.y--;
  } else if (direction === "down") {
    head.y++;
  } else if (direction === "left") {
    head.x--;
  } else if (direction === "right") {
    head.x++;
  }
  console.log(head);
  snake.unshift(head);

  const removedSegment = snake.pop();
  const newHead = document.createElement("div");
  newHead.classList.add("snake");
  newHead.style.left = head.x * 20 + "px";
  newHead.style.top = head.y * 20 + "px";
  gameArea.insertBefore(newHead, gameArea.firstChild);
 
  if (removedSegment) {
    const elementsToDelete = document.querySelectorAll(".snake");
    if (elementsToDelete.length > 0) {
      const lastElement = elementsToDelete[elementsToDelete.length - 1];
      gameArea.removeChild(lastElement);
    }
  }
}

let food = { x: 5, y: 5 };

function generateFood() {
  let foodItem = document.querySelectorAll(".food");
  if (foodItem.length >= 1) {
    gameArea.removeChild(foodItem[0]);
  }

  food.x = Math.floor(Math.random() * 20);
  food.y = Math.floor(Math.random() * 20);
  if (food.x <= 0) {
    food.x = 1;
  } else if (food.x >= 19) {
    food.x = 18;
  }

  if (food.y <= 0) {
    food.y = 1;
  } else if (food.y >= 19) {
    food.y = 18;
  }

  const foodElement = document.createElement("div");
  foodElement.classList.add("food");
  foodElement.style.left = food.x * 20 + "px";
  foodElement.style.top = food.y * 20 + "px";
  gameArea.appendChild(foodElement);
}

generateFood();

function checkCollision() {
    const head = snake[0];
  
    if (head.x < 0 || head.x >= 20 || head.y < 0 || head.y >= 20) {
      clearInterval(gameInterval);
      alert("游戏结束！");
      return true;
    }
  
    console.log("head:", head.x, head.y);
    console.log("food:", food.x, food.y);
    if (head.x === food.x && head.y === food.y) {
      let x, y;

      if (direction === "right") {
        x = snake[snake.length - 1].x - 1;
        y = snake[snake.length - 1].y;
      } else if (direction === "left") {
        x = snake[snake.length - 1].x + 1;
        y = snake[snake.length - 1].y;
      } else if (direction === "top") {
        x = snake[snake.length - 1].x;
        y = snake[snake.length - 1].y + 1;
      } else {
        x = snake[snake.length - 1].x;
        y = snake[snake.length - 1].y - 1;
      }
  
      snake.push({ x, y });
      const newTail = document.createElement("div");
      newTail.classList.add("snake");
      newTail.style.left = x * 20 + "px";
      newTail.style.top = y * 20 + "px";
      gameArea.appendChild(newTail);
      generateFood();
    }
  
    for (let i = 1; i < snake.length; i++) {
      if (head.x === snake[i].x && head.y === snake[i].y) {
        clearInterval(gameInterval); 
        alert("游戏结束！");
        return true;
      }
    }
    return false;
  }
  
  let gameInterval = setInterval(() => {
    if (!checkCollision()) {
      moveSnake();
    }
  }, 300);
  

