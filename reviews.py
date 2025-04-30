import requests
import time

class OGSReviews:
    API_BASE = "https://online-go.com/api/v1"

    @staticmethod
    def get_json(url):
        """ Fetch JSON from the given URL, handling errors """
        try:
            response = requests.get(url)
            if response.status_code == 429:
                print("429 Too Many Requests - Sleeping...")
                time.sleep(5)
                return OGSReviews.get_json(url)  # Retry after sleep

            response.raise_for_status()
            return response.json()
        except requests.exceptions.RequestException as e:
            print("Error fetching data:", e)
            return None

    @staticmethod
    def list_authored_reviews(player_id):
        """ List all reviews authored by the given player """
        reviews_url = f"{OGSReviews.API_BASE}/players/{player_id}/reviews"
        reviews = OGSReviews.get_json(reviews_url)

        if not reviews or "results" not in reviews:
            print(f"No authored reviews found for player ID: {player_id}")
            return

        print(f"Found {reviews.get('count', 0)} reviews authored by player {player_id}.")
        next_page = reviews.get("next")

        while next_page:
            for review in reviews["results"]:
                game_id = review.get("game", {}).get("id")
                game_name = review.get("game", {}).get("name")
                created_at = review.get("created_at", "Unknown")
                print(f"Reviewed Game {game_id}: {game_name} (Created at: {created_at})")

            reviews = OGSReviews.get_json(next_page)
            next_page = reviews.get("next") if reviews else None

    @staticmethod
    def main(player_id):
        """ Start listing authored reviews for the given player ID """
        OGSReviews.list_authored_reviews(player_id)

# Replace this with your actual OGS player ID
if __name__ == "__main__":
    player_id = 179  # Example: Your player ID
    OGSReviews.main(player_id)

